package dev.bober.finik.core.data

import dev.bober.finik.core.model.*
import dev.bober.finik.core.network.*
import dev.bober.finik.core.network.dto.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.SerializationException
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

data class CareEvent(val category: SpendCategory, val sequence: Long)
data class ToastEvent(val text: String, val warning: Boolean = false)

/** Server owns every game mutation. Room is a separate read-only cache for each session. */
class FinikRepository(
    private val cacheStore: SnapshotStore,
    private val apiFactory: (String, suspend () -> String?) -> FinikApi,
    private val session: PlayerSession,
    private val deviceIds: DeviceIdProvider,
    private val networkConfig: NetworkConfig,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) {
    private var currentApi: FinikApi? = null
    private val api: FinikApi get() = checkNotNull(currentApi)
    private var endpointInitialized = false
    private val _serverAddress = MutableStateFlow(networkConfig.normalizedBaseUrl)
    val serverAddress = _serverAddress.asStateFlow()
    val canChangeServer = networkConfig.canChangeServer
    private val scope = CoroutineScope(SupervisorJob() + io)
    private val requests = Mutex()
    private var mode = "normal"
    private var cache: ServerCache? = null
    private var careSequence = 0L
    private val _state = MutableStateFlow(emptyState().copy(ready = false))
    val state = _state.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    val loadProblem = error
    private val _extrasEnabled = MutableStateFlow(false)
    val extrasEnabled = _extrasEnabled.asStateFlow()
    private val _toasts = MutableSharedFlow<ToastEvent>(extraBufferCapacity = 8)
    val toasts = _toasts.asSharedFlow()
    private val _careEvents = MutableSharedFlow<CareEvent>(extraBufferCapacity = 8)
    val careEvents = _careEvents.asSharedFlow()
    var glossary: List<GlossaryTerm> = emptyList()
        private set

    init { scope.launch { boot() } }

    private fun emptyState(demo: Boolean = false) = SampleData.snapshot.copy(
        onboarded = false, ready = true, online = false, demoMode = demo,
        shop = emptyList(), tasks = emptyList(), history = emptyList(), badges = emptyList(),
        report = null, weekLog = emptyList(), transactions = emptyList(),
        goals = SampleData.goals.map { it.copy(saved = 0) },
    )

    private suspend fun boot() = request(requireOnline = false) {
        mode = session.mode()
        restoreCache(mode)
        authenticate(mode)
    }

    private suspend fun initializeEndpoint() {
        if (endpointInitialized) return
        val saved = if (canChangeServer) session.serverAddress() else null
        val address = saved?.let { runCatching { NetworkConfig.normalize(it) }.getOrNull() }
            ?: networkConfig.normalizedBaseUrl
        if (session.boundServer() != address) {
            session.selectServer(address, saveOverride = false)
            cacheStore.clear(1)
            cacheStore.clear(2)
        }
        _serverAddress.value = address
        currentApi = authenticatedApi(address)
        endpointInitialized = true
    }

    private fun authenticatedApi(address: String): FinikApi = apiFactory(address) {
        // Session tokens belong to one server, including across process restarts.
        if (session.boundServer() == address) session.token() else null
    }

    suspend fun connectToServer(rawAddress: String): Pair<Boolean, String> =
        scope.async { switchServer(rawAddress) }.await()

    private suspend fun switchServer(rawAddress: String): Pair<Boolean, String> {
        if (!canChangeServer) return false to "Адрес сервера в этой сборке задаётся разработчиком."
        if (!requests.tryLock()) return false to "Подожди завершения текущего действия."
        _busy.value = true
        var applied = false
        return try {
            withContext(io) {
                val address = NetworkConfig.normalizeUserAddress(rawAddress)
                check(networkConfig.copy(baseUrl = address).canConnect) { "Для этой сборки нужен HTTPS-адрес сервера." }
                // The health probe is anonymous. Later requests may use only the candidate's token.
                var candidateToken: String? = null
                val probe = apiFactory(address) { candidateToken }
                val targetMode = session.mode()
                val prepared: PreparedLogin
                try {
                    check(probe.health()) { "Сервер не подтвердил готовность. Проверь адрес API Finik." }
                    val login = probe.login(LoginIn(deviceIds.get(), ZoneId.systemDefault().id, targetMode))
                    check(login.token.isNotBlank() && login.mode == targetMode) { "Сервер вернул неверную сессию Finik." }
                    candidateToken = login.token
                    prepared = if (login.hasPet) PreparedLogin(login, state = probe.getState())
                        else PreparedLogin(login, goals = probe.goalCatalog())
                } finally { probe.close() }

                initializeEndpoint()
                mode = targetMode
                if (address != _serverAddress.value) {
                    val oldApi = currentApi
                    val newApi = authenticatedApi(address)
                    try {
                        // The caller can leave its screen while this short commit completes.
                        withContext(NonCancellable) {
                            session.selectServer(address, saveOverride = true)
                            applied = true
                            _serverAddress.value = address
                            currentApi = newApi
                            cache = null
                            glossary = emptyList()
                            _state.value = emptyState(mode == "demo").copy(ready = false)
                            _extrasEnabled.value = false
                            oldApi?.close()
                        }
                    } catch (e: Exception) {
                        if (!applied) newApi.close()
                        throw e
                    }
                    // Endpoint-tagged snapshots cannot be read at the new origin even if deletion fails.
                    cacheStore.clear(1)
                    cacheStore.clear(2)
                } else {
                    // Rechecking the same address reconnects without deleting either profile cache.
                    session.saveServerAddress(address)
                }
                applied = true
                _error.value = null
                authenticate(mode, prepared = prepared)
                true to "Сервер подключён: $address"
            }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            if (applied) {
                markFailure(e)
                false to "Адрес сохранён: ${_serverAddress.value}. Войти пока не удалось. ${_error.value.orEmpty()}"
            } else {
                val message = if (e is IllegalArgumentException || e is IllegalStateException) e.message.orEmpty()
                    else "Сервер недоступен. Проверь адрес, сеть и запущен ли API."
                false to "$message Текущий сервер не изменён."
            }
        } finally { _busy.value = false; requests.unlock() }
    }

    private data class PreparedLogin(
        val login: LoginOut,
        val state: StateOut? = null,
        val goals: List<GoalCatalogOut>? = null,
    )

    private suspend fun restoreCache(targetMode: String) {
        val saved = cacheStore.read(cacheId(targetMode))
        cache = saved?.let {
            try { FinikJson.json.decodeFromString<ServerCache>(it).takeIf { saved -> saved.serverAddress == _serverAddress.value } }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { null }
        }
        _state.value = cache?.toSnapshot(emptyState(targetMode == "demo"), online = false)
            ?: emptyState(targetMode == "demo")
        glossary = cache?.terms?.map { it.toModel() }.orEmpty()
    }

    suspend fun refresh() = request(requireOnline = false) { authenticate(mode) }
    suspend fun retryLoad() = refresh()

    private suspend fun authenticate(targetMode: String, preset: String? = null, prepared: PreparedLogin? = null) {
        checkConnectionConfigured()
        val result = prepared?.login ?: api.login(LoginIn(deviceIds.get(), ZoneId.systemDefault().id, targetMode, preset))
        session.activate(targetMode, result.token)
        val changedMode = targetMode != mode || preset != null
        mode = targetMode
        if (changedMode) {
            // A reset demo must never display an earlier demo cache or normal profile.
            if (preset != null) cacheStore.clear(cacheId(mode))
            restoreCache(mode)
        }
        if (result.hasPet) {
            acceptState(prepared?.state ?: api.getState())
            refreshLists()
            refreshHomeMaterials()
        } else {
            cache = null
            cacheStore.clear(cacheId(mode))
            val goals = (prepared?.goals ?: api.goalCatalog()).map { it.toModel() }
            _state.value = emptyState(mode == "demo").copy(
                online = true, timezone = result.timezone,
                goals = goals.ifEmpty { SampleData.goals.map { it.copy(saved = 0) } },
                selectedGoalId = goals.firstOrNull()?.id ?: SampleData.goals.first().id,
            )
        }
        _extrasEnabled.value = _state.value.onboarded
    }

    suspend fun startDemo(prepared: Boolean) = request(requireOnline = false) {
        authenticate("demo", if (prepared) "prepared" else "new")
    }
    suspend fun leaveDemo() = request(requireOnline = false) { authenticate("normal") }

    suspend fun createProfile(name: String, species: PetSpecies, appearance: PetAppearance, income: Int) = request {
        if (_state.value.onboarded) return@request
        val result = api.createPet(PetIn(
            name.trim(), species.legacyApiName, income,
            _state.value.goals.firstOrNull()?.catalogSlug,
            appearance.furColor.ordinal,
            appearance.accessories.map { it.id },
        ))
        finishAction(result, "Привет, ${result.pet.name}! Распредели недельный доход.")
        refreshHomeMaterials()
    }

    suspend fun changePlan(category: SpendCategory, delta: Int) = request {
        val current = _state.value
        if (current.planConfirmed) {
            require(delta > 0) { "Подтверждённый план можно только пополнять свободными монетами." }
            finishAction(api.topUpPlan(planAmounts(mapOf(category to delta))))
        } else {
            val amounts = current.plan.entries.associate { it.category to it.planned }.toMutableMap()
            amounts[category] = (amounts[category] ?: 0) + delta
            require(amounts.getValue(category) >= 0) { "В статье уже нет монет." }
            finishAction(api.setPlan(planAmounts(amounts)))
        }
    }

    suspend fun applyAdvice() = request {
        check(!_state.value.planConfirmed) { "План уже подтверждён. Можно пополнить отдельные статьи." }
        val available = _state.value.plan.freeCoins + _state.value.plan.total
        val food = (available * .4).roundToInt()
        val water = (available * .3).roundToInt()
        val play = (available * .1).roundToInt()
        finishAction(api.setPlan(PlanIn(food, water, play, available - food - water - play)), "План 40/30/10/20 готов к подтверждению.")
    }
    suspend fun resetPlan() = request {
        check(!_state.value.planConfirmed) { "Подтверждённый план нельзя сбросить." }
        finishAction(api.setPlan(PlanIn(0, 0, 0, 0)))
    }
    suspend fun confirmPlan() = request { finishAction(api.confirmPlan(), "План подтверждён.") }
    suspend fun care(category: SpendCategory) = request {
        val out = api.care(CareIn(category.name))
        finishAction(out.state, "+${out.restored} к потребности, +${out.xpGained} XP." + if (out.fromSavings) " Оплачено из копилки." else "")
        _careEvents.emit(CareEvent(category, ++careSequence))
    }
    suspend fun buy(itemId: String) = request {
        val out = api.buy(BuyIn(itemId))
        finishAction(out.state, "Купили «${out.name.animalText()}».")
    }
    suspend fun customize(pot: String? = null, accessory: String? = null, lookVariant: Int? = null) = request {
        finishAction(api.customize(CustomizeIn(pot, accessory, lookVariant)))
    }
    suspend fun customizeAppearance(appearance: PetAppearance) = request {
        finishAction(api.customize(CustomizeIn(lookVariant = appearance.furColor.ordinal, accessories = appearance.accessories.map { it.id })), "Внешность сохранена.")
    }
    suspend fun deposit(amount: Int) = request { finishAction(api.deposit(DepositIn(amount))) }
    suspend fun withdraw(amount: Int) = request { finishAction(api.withdraw(DepositIn(amount))) }
    suspend fun selectGoal(id: String) = request {
        val slug = _state.value.goals.firstOrNull { it.id == id }?.catalogSlug ?: id
        finishAction(api.selectGoal(GoalIn(slug)))
    }
    suspend fun taskQuestions(slug: String): List<QuizQuestion> = readRequest {
        api.questions(slug).map { it.toModel() }
    }.orEmpty()
    suspend fun answerTask(slug: String, questionSlug: String, answerIndex: Int? = null, answerValue: Int? = null): LessonAnswer? = request {
        val out = api.answer(slug, AnswerIn(questionSlug, answerIndex, answerValue))
        refreshListsAfterAction()
        out.toModel()
    }
    suspend fun claimTask(slug: String) = request { finishAction(api.claim(slug), "Награда поступила в свободные монеты.") }
    suspend fun taskHint(slug: String, questionSlug: String): String? = request { api.taskHint(slug, HintIn(questionSlug)).text }

    suspend fun adventure(slug: String): Adventure? = readRequest { api.adventure(slug).toModel() }
    suspend fun playAdventure(slug: String, action: AdventureAction): Adventure? = request {
        val out = api.playAdventure(slug, AdventureActionIn(action.expectedStage, action.food, action.water, action.reserve, action.choiceId, action.amount))
        refreshListsAfterAction()
        out.toModel()
    }
    suspend fun restartAdventure(slug: String): Adventure? = request { api.restartAdventure(slug).toModel() }
    suspend fun review(): LearningReview? = readRequest { api.review().toModel() }
    suspend fun answerReview(topic: String, answerIndex: Int): ReviewAnswer? = request { api.answerReview(ReviewAnswerIn(topic, answerIndex)).toModel() }

    suspend fun advanceDemoDay() = request {
        check(_state.value.demoMode && _state.value.canAdvanceTime) { "Пропуск времени доступен только в демо." }
        finishAction(api.advanceDemo(DemoAdvanceIn(days = 1)), "В демо наступил следующий день.")
    }
    suspend fun closeWeek(): Boolean = request {
        check(_state.value.demoMode && _state.value.canAdvanceTime) { "Неделя завершится автоматически по местному времени. Ускорение доступно только в демо." }
        finishAction(api.advanceDemo(DemoAdvanceIn(toWeekEnd = true)), "Демонстрационная неделя завершена.")
        _state.value.report != null
    } ?: false
    suspend fun repeatLastPlan() = request {
        val report = _state.value.report ?: error("Ещё нет завершённой недели.")
        check(!_state.value.planConfirmed) { "Текущий план уже подтверждён." }
        finishAction(api.setPlan(planAmounts(report.rows.associate { it.category to it.planned })))
    }
    suspend fun parentBonus(amount: Int = 10, reason: String = "Поддержка родителя", pin: String = "") = request {
        require(pin.isNotBlank()) { "Нужен PIN родителя." }
        require(amount in 1..20) { "Можно начислить от 1 до 20 монет." }
        finishAction(api.parentBonus(BonusIn(amount, reason.take(80), pin)), "Начислено $amount монет.")
    }
    suspend fun setSound(on: Boolean) = request { finishAction(api.updateProfile(SettingsIn(soundOn = on))) }
    suspend fun setMotion(on: Boolean) = request(requireOnline = false) {
        // Motion is a device display preference and never changes game progress.
        _state.value = _state.value.copy(motionOn = on)
        cache = cache?.copy(motionOn = on)
        persist()
    }
    suspend fun setIncome(income: Int) = request { finishAction(api.updateProfile(SettingsIn(weeklyIncome = income)), "Новый доход действует со следующей недели.") }
    suspend fun setEventMode(eventMode: String) = request { finishAction(api.updateProfile(SettingsIn(eventMode = eventMode))) }
    suspend fun setOnlineExtras(enabled: Boolean) {
        if (enabled) refresh() else _toasts.emit(ToastEvent("Игра использует сервер. Без сети доступен просмотр сохранённого состояния."))
    }
    fun buyCheck(itemId: String): BuyCheck? {
        val item = _state.value.shop.firstOrNull { it.id == itemId } ?: return null
        return when {
            !_state.value.online -> BuyCheck(false, "Для покупки нужно подключение к серверу.")
            item.owned -> BuyCheck(false, "Этот предмет уже куплен.")
            !item.affordable -> BuyCheck(false, "Не хватает монет в статье «${item.category.label}».")
            else -> BuyCheck(true, "Купить «${item.name}» за ${item.cost} монет?")
        }
    }
    suspend fun resetProfile(): Boolean = request {
        // There is no local-only progress to reset. Keep the device identity for the server account.
        api.reset()
        cacheStore.clear(cacheId(mode))
        cache = null
        _state.value = emptyState(mode == "demo").copy(online = true)
        _extrasEnabled.value = false
        true
    } ?: false

    suspend fun todayEvent(): TodayEvent? = readRequest {
        val event = api.todayEvent()
        cache = cache?.copy(event = event)
        publish()
        event?.toModel()
    }
    suspend fun chooseEvent(id: String, option: String): EventChoice? = request {
        val out = api.chooseEvent(id, ChooseIn(option))
        cache = cache?.copy(event = cache?.event?.copy(chosen = option))
        finishAction(out.state, out.note.animalText())
        out.toModel()
    }
    suspend fun wordOfDay(): WordOfDay? = readRequest { api.wordOfDay().toModel() }
    suspend fun diary(): DiaryEntry? = readRequest { api.diary().toModel() }
    suspend fun dreamPlan(): DreamPlan? = readRequest { api.dreamPlan().toModel() }
    suspend fun origin(): OriginStory? = readRequest { api.origin().toModel() }
    suspend fun weekSummary(): String? = readRequest { api.weekSummary().text }
    suspend fun aiQuiz(kind: String = "quiz"): AiQuiz? = readRequest { api.quiz(kind).toModel() }
    suspend fun answerQuiz(index: Int, answerIndex: Int, kind: String = "quiz"): AiQuizAnswer? = request {
        val out = api.answerQuiz(QuizAnswerIn(index, answerIndex, kind))
        // The quiz endpoint returns a reward rather than state. Only a GET reconciles its balance.
        try { finishAction(api.getState()) } catch (e: CancellationException) { throw e }
        catch (e: Exception) { markFailure(e, "Ответ принят. Обнови состояние, чтобы увидеть награду.") }
        out.toModel()
    }
    suspend fun chat(text: String): ChatReply? = request { api.chat(ChatIn(text)).toModel() }

    private suspend fun refreshHomeMaterials() = supervisorScope {
        // Optional generated text cannot roll back a loaded game.
        val word = async { optional { api.wordOfDay() } }
        val remark = async { optional { api.remark() } }
        val loadedWord = word.await()
        val loadedRemark = remark.await()
        cache = cache?.let { current -> current.copy(
            word = loadedWord ?: current.word,
            state = loadedRemark?.let { current.state.copy(pet = current.state.pet.copy(moodNote = it.text)) } ?: current.state,
        ) }
        publish()
    }
    private suspend fun <T> optional(block: suspend () -> T): T? = try { block() }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { null }

    private suspend fun acceptState(out: StateOut) {
        cache = cache?.copy(state = out, cachedAt = Instant.now().toString())
            ?: ServerCache(serverAddress = _serverAddress.value, state = out, cachedAt = Instant.now().toString())
        publish()
    }
    private suspend fun publish() {
        val current = cache ?: return
        _state.value = current.toSnapshot(emptyState(mode == "demo"), online = true)
        glossary = current.terms.map { it.toModel() }
        _extrasEnabled.value = true
        persist()
    }
    private suspend fun persist() {
        cache?.let { cacheStore.write(cacheId(mode), FinikJson.json.encodeToString(ServerCache.serializer(), it)) }
    }
    private suspend fun finishAction(out: StateOut, message: String? = null) {
        acceptState(out)
        if (!message.isNullOrBlank()) _toasts.emit(ToastEvent(message))
        refreshListsAfterAction()
    }
    private suspend fun refreshListsAfterAction() {
        try { refreshLists() }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) { markFailure(e, "Действие сохранено на сервере. Не удалось обновить все данные; нажми «Обновить».") }
    }
    private suspend fun refreshLists() = coroutineScope {
        val shop = async { api.shopItems() }
        val tasks = async { api.tasks() }
        val profile = async { api.profile() }
        val history = async { api.history() }
        val badges = async { api.badges() }
        val terms = async { api.terms() }
        val event = async { api.todayEvent() }
        val loadedProfile = profile.await()
        cache = cache?.copy(
            state = loadedProfile.state ?: cache!!.state,
            shop = shop.await(), tasks = tasks.await(), profile = loadedProfile,
            history = history.await(), badges = badges.await(), terms = terms.await(), event = event.await(),
            cachedAt = Instant.now().toString(),
        )
        publish()
    }

    private suspend fun <T> readRequest(block: suspend () -> T): T? = request(requireOnline = false) {
        // An explicit retry in a content sheet can reconnect without closing the sheet.
        // Authentication resumes this profile; it never supplies a demo reset preset.
        if (!_state.value.online) authenticate(mode)
        block()
    }

    private suspend fun <T> request(requireOnline: Boolean = true, block: suspend () -> T): T? {
        // Drop repeated taps while a request is in flight; never queue financial commands offline.
        if (!requests.tryLock()) return null
        _busy.value = true
        _error.value = null
        return try {
            initializeEndpoint()
            if (requireOnline) check(_state.value.online) { "Без сети доступен только просмотр. Нажми «Обновить» после подключения." }
            withContext(io) { block() }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { markFailure(e); null }
        finally { _busy.value = false; requests.unlock() }
    }
    private fun checkConnectionConfigured() {
        val activeConfig = networkConfig.copy(baseUrl = _serverAddress.value)
        check(activeConfig.canConnect) { if (!activeConfig.isConfigured) "Адрес сервера не настроен." else "Для этой сборки нужен HTTPS-адрес сервера." }
    }
    private suspend fun markFailure(error: Exception, override: String? = null) {
        val unavailable = error is SerializationException || error !is IllegalArgumentException && error !is IllegalStateException &&
            (error !is FinikApiException || error.httpStatus == 401 || error.httpStatus >= 500)
        if (unavailable) {
            _state.value = _state.value.copy(online = false, ready = true)
            _extrasEnabled.value = false
        }
        if (error is FinikApiException && error.httpStatus == 401) session.clearToken()
        val text = override ?: when {
            error is SerializationException -> "Ответ сервера не удалось прочитать. Обнови состояние перед повтором действия."
            error is IllegalArgumentException || error is IllegalStateException -> error.message.orEmpty()
            error is FinikApiException -> error.userMessage()
            else -> "Нет связи с сервером. Доступен просмотр сохранённого состояния. Обнови его перед повтором действия."
        }
        _error.value = text
        _toasts.emit(ToastEvent(text, warning = true))
    }
    private fun cacheId(mode: String) = if (mode == "demo") 2 else 1
    private fun planAmounts(amounts: Map<SpendCategory, Int>) = PlanIn(
        amounts[SpendCategory.FOOD] ?: 0, amounts[SpendCategory.WATER] ?: 0,
        amounts[SpendCategory.PLAY] ?: 0, amounts[SpendCategory.SAVE] ?: 0,
    )
}
