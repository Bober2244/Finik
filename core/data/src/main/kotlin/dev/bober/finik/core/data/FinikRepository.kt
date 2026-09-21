package dev.bober.finik.core.data

import dev.bober.finik.core.data.content.ContentCatalog
import dev.bober.finik.core.data.game.GameEngine
import dev.bober.finik.core.data.snapshot.decodeSnapshot
import dev.bober.finik.core.data.snapshot.encode
import dev.bober.finik.core.database.FinikDatabase
import dev.bober.finik.core.database.StateEntity
import dev.bober.finik.core.model.AiQuiz
import dev.bober.finik.core.model.AiQuizAnswer
import dev.bober.finik.core.model.BuyCheck
import dev.bober.finik.core.model.ChatReply
import dev.bober.finik.core.model.DiaryEntry
import dev.bober.finik.core.model.DreamPlan
import dev.bober.finik.core.model.EngineResult
import dev.bober.finik.core.model.EventChoice
import dev.bober.finik.core.model.GameSnapshot
import dev.bober.finik.core.model.GlossaryTerm
import dev.bober.finik.core.model.LessonAnswer
import dev.bober.finik.core.model.OriginStory
import dev.bober.finik.core.model.PetMood
import dev.bober.finik.core.model.PetPotStyle
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.core.model.QuizQuestion
import dev.bober.finik.core.model.SampleData
import dev.bober.finik.core.model.SpendCategory
import dev.bober.finik.core.model.TodayEvent
import dev.bober.finik.core.model.WordOfDay
import dev.bober.finik.core.network.FinikApi
import dev.bober.finik.core.network.FinikApiException
import dev.bober.finik.core.network.FinikJson
import dev.bober.finik.core.network.dto.AnswerIn
import dev.bober.finik.core.network.dto.BonusIn
import dev.bober.finik.core.network.dto.BuyIn
import dev.bober.finik.core.network.dto.CareIn
import dev.bober.finik.core.network.dto.ChatIn
import dev.bober.finik.core.network.dto.ChooseIn
import dev.bober.finik.core.network.dto.CustomizeIn
import dev.bober.finik.core.network.dto.DepositIn
import dev.bober.finik.core.network.dto.GoalIn
import dev.bober.finik.core.network.dto.LoginIn
import dev.bober.finik.core.network.dto.PetIn
import dev.bober.finik.core.network.dto.PlanIn
import dev.bober.finik.core.network.dto.QuizAnswerIn
import dev.bober.finik.core.network.dto.SettingsIn
import dev.bober.finik.core.network.dto.StateOut
import dev.bober.finik.core.network.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt

data class ToastEvent(val text: String, val warning: Boolean = false)

class FinikRepository(
    private val db: FinikDatabase,
    private val api: FinikApi,
    private val session: SessionStore,
    private val deviceIds: DeviceIdProvider,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) {
    private val scope = CoroutineScope(SupervisorJob() + io)
    private var catalog = ContentCatalog()
    private var remoteEnabled = false

    private val _state = MutableStateFlow(SampleData.snapshot.copy(onboarded = false, ready = false))
    val state: StateFlow<GameSnapshot> = _state.asStateFlow()

    private val _toasts = MutableSharedFlow<ToastEvent>(extraBufferCapacity = 8)
    val toasts: SharedFlow<ToastEvent> = _toasts.asSharedFlow()

    var glossary: List<GlossaryTerm> = catalog.glossary
        private set
    val scenarios get() = catalog.scenarios
    val quiz get() = catalog.quiz

    init {
        scope.launch { boot() }
    }

    private suspend fun boot() {
        catalog = ContentCatalog()
        glossary = catalog.glossary
        val stored = db.stateDao().get()?.json
        val loaded = if (stored != null) {
            runCatching { decodeSnapshot(stored, catalog) }.getOrNull()
        } else {
            null
        }
        _state.value = (loaded ?: SampleData.snapshot.copy(onboarded = false, ready = true))
            .copy(shop = if (loaded == null) catalog.shop else loaded.shop, ready = true)
        if (_state.value.onboarded) {
            apply { GameEngine.dailyOpen(this, today()) }
        }
        connectBackend()
    }

    private suspend fun connectBackend() {
        val login = try {
            withContext(io) {
                val result = api.login(LoginIn(deviceIds.get()))
                session.saveToken(result.token)
                result
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            remoteEnabled = false
            return
        }
        if (login.hasPet) {
            remoteEnabled = true
            val remote = runCatchingUser { api.getState() } ?: return
            applyRemoteState(remote)
            refreshRemoteLists()
            refreshHomeExtras()
            return
        }
        refreshCatalogs()
        if (_state.value.onboarded) {
            remoteEnabled = false
            _state.value = _state.value.copy(online = false)
        } else {
            remoteEnabled = true
            _state.value = _state.value.copy(online = true)
        }
    }

    suspend fun createProfile(
        name: String,
        species: PetSpecies,
        potStyle: PetPotStyle,
        income: Int,
    ) {
        if (ensureRemoteLogin()) {
            val created = runCatchingUser {
                try {
                    api.createPet(
                        PetIn(
                            name = name.trim(),
                            species = species.name,
                            weeklyIncome = income,
                            goalSlug = _state.value.goals.firstOrNull()?.catalogSlug,
                            lookVariant = potStyle.toLookVariant(),
                        ),
                    )
                } catch (e: FinikApiException) {
                    if (e.code == "conflict" || e.httpStatus == 409) api.getState() else throw e
                }
            }
            if (created != null) {
                remoteEnabled = true
                applyRemoteState(created, "Привет, ${created.pet.name}! Разложи ${created.weeklyIncome} монет: нужное, желаемое, копилка.")
                refreshRemoteLists()
                return
            }
        }
        val created = GameEngine.createProfile(
            name = name,
            species = species,
            potStyle = potStyle,
            income = income,
            shop = catalog.shop,
            goals = catalog.goals,
            tasks = catalog.tasks,
            epochDay = today(),
        )
        commit(EngineResult(created, "Привет, ${created.pet.name}! Разложи ${created.plan.weeklyIncome} монет: нужное, желаемое, копилка."))
    }

    suspend fun changePlan(category: SpendCategory, delta: Int) {
        val preview = GameEngine.changePlan(_state.value, category, delta)
        if (preview.warning || !remoteEnabled) {
            commit(preview)
            return
        }
        applyRemoteState(runCatchingUser { api.setPlan(preview.state.plan.toPlanIn()) } ?: return, preview.message)
    }

    suspend fun applyAdvice() {
        if (remoteEnabled) {
            if (_state.value.planConfirmed) {
                _toasts.emit(ToastEvent("План уже подтверждён.", warning = true))
                return
            }
            val income = _state.value.plan.weeklyIncome
            val food = (income * 0.40).roundToInt()
            val water = (income * 0.30).roundToInt()
            val play = (income * 0.10).roundToInt()
            val save = (income - food - water - play).coerceAtLeast(0)
            applyRemoteState(
                runCatchingUser { api.setPlan(PlanIn(food, water, play, save)) } ?: return,
                "Совет 40/30/10/20: еда $food, вода $water, игры $play, копилка $save.",
            )
            return
        }
        apply { GameEngine.applyAdvice(this) }
    }

    suspend fun resetPlan() {
        if (remoteEnabled) {
            if (_state.value.planConfirmed) {
                _toasts.emit(ToastEvent("После подтверждения план не сбрасывается.", warning = true))
                return
            }
            applyRemoteState(runCatchingUser { api.setPlan(PlanIn(0, 0, 0, 0)) } ?: return, "План очищен. Разложи монеты заново.")
            return
        }
        apply { GameEngine.resetPlan(this) }
    }

    suspend fun confirmPlan() {
        if (remoteEnabled) {
            applyRemoteState(runCatchingUser { api.confirmPlan() } ?: return, "План утверждён.")
            refreshRemoteLists()
            return
        }
        apply { GameEngine.confirmPlan(this) }
    }

    suspend fun care(category: SpendCategory) {
        if (remoteEnabled) {
            val out = runCatchingUser { api.care(CareIn(category.name)) } ?: return
            val label = _state.value.care.firstOrNull { it.category == category }?.label ?: category.label
            val extra = if (out.fromSavings) " Добрали из копилки." else ""
            applyRemoteState(out.state, "$label. +${out.xpGained} опыта.$extra")
            return
        }
        apply { GameEngine.care(this, category) }
    }

    suspend fun buy(itemId: String) {
        if (remoteEnabled) {
            val out = runCatchingUser { api.buy(BuyIn(itemId)) } ?: return
            applyRemoteState(out.state, "Купили «${out.name}».")
            refreshShopAndTasks()
            return
        }
        apply { GameEngine.buy(this, itemId) }
    }

    suspend fun customize(pot: String? = null, accessory: String? = null, lookVariant: Int? = null) {
        if (!remoteEnabled) return
        applyRemoteState(runCatchingUser { api.customize(CustomizeIn(pot, accessory, lookVariant)) } ?: return)
    }

    suspend fun deposit(amount: Int) {
        if (remoteEnabled) {
            applyRemoteState(runCatchingUser { api.deposit(DepositIn(amount)) } ?: return)
            return
        }
        apply { GameEngine.deposit(this, amount) }
    }

    suspend fun withdraw(amount: Int) {
        if (remoteEnabled) {
            _toasts.emit(ToastEvent("Из копилки на сервере снимать нельзя — только на мечту.", warning = true))
            return
        }
        apply { GameEngine.withdraw(this, amount) }
    }

    suspend fun selectGoal(id: String) {
        if (remoteEnabled) {
            val slug = _state.value.goals.firstOrNull { it.id == id }?.catalogSlug ?: id
            applyRemoteState(runCatchingUser { api.selectGoal(GoalIn(slug)) } ?: return)
            return
        }
        apply { GameEngine.selectGoal(this, id) }
    }

    suspend fun completeTask(taskId: String, correct: Boolean, reward: Int) {
        apply { GameEngine.completeTask(this, taskId, correct, reward) }
        if (remoteEnabled && correct) {
            runCatching { withContext(io) { api.claim(taskId) } }
                .onSuccess { applyRemoteState(it) }
            refreshShopAndTasks()
        }
    }

    suspend fun taskQuestions(slug: String): List<QuizQuestion> =
        runCatchingUser { api.questions(slug) }?.map { it.toModel() }.orEmpty()

    suspend fun answerTask(slug: String, questionSlug: String, answerIndex: Int): LessonAnswer? {
        val out = runCatchingUser { api.answer(slug, AnswerIn(questionSlug, answerIndex)) } ?: return null
        return out.toModel()
    }

    suspend fun claimTask(slug: String) {
        if (!remoteEnabled) return
        applyRemoteState(runCatchingUser { api.claim(slug) } ?: return)
        refreshShopAndTasks()
    }

    suspend fun closeWeek() {
        if (remoteEnabled) {
            val day = runCatchingUser { api.endDay() } ?: return
            applyRemoteState(day.state)
            val next = _state.value.copy(wilted = day.wilted, wiltXpLost = day.wiltXpLost)
            persist(next)
            _state.value = next
            if (day.weekFinished) refreshRemoteLists()
            val text = buildString {
                if (day.weekFinished) {
                    append("Неделя закрыта.")
                    day.weekReport?.let { report ->
                        if (report.interest > 0) append(" Проценты +${report.interest}.")
                        if (report.cashback > 0) append(" Кэшбэк +${report.cashback}.")
                    }
                } else {
                    append("День ${day.day}. +${day.xpGained} опыта.")
                }
                if (day.wilted) append(" Росток подвял.")
            }
            if (text.isNotBlank()) _toasts.emit(ToastEvent(text, warning = day.wilted))
            return
        }
        apply { GameEngine.closeWeek(this) }
    }

    suspend fun repeatLastPlan() {
        if (!remoteEnabled) {
            apply { GameEngine.repeatLastPlan(this, report) }
            return
        }
        val report = _state.value.report
        if (report == null) {
            apply { GameEngine.repeatLastPlan(this, report) }
            return
        }
        val food = report.rows.firstOrNull { it.category == SpendCategory.FOOD }?.planned ?: 0
        val water = report.rows.firstOrNull { it.category == SpendCategory.WATER }?.planned ?: 0
        val play = report.rows.firstOrNull { it.category == SpendCategory.PLAY }?.planned ?: 0
        val save = report.rows.firstOrNull { it.category == SpendCategory.SAVE }?.planned ?: 0
        applyRemoteState(
            runCatchingUser { api.setPlan(PlanIn(food, water, play, save)) } ?: return,
            "Повторили прошлый план. Проверь и подтверди.",
        )
    }

    suspend fun parentBonus(amount: Int = 10, reason: String = "поддержка", pin: String = "") {
        if (remoteEnabled) {
            if (pin.isBlank()) {
                _toasts.emit(ToastEvent("Нужен код родителя", warning = true))
                return
            }
            applyRemoteState(
                runCatchingUser {
                    api.parentBonus(BonusIn(amount.coerceIn(1, 20), reason.take(80), pin.take(16)))
                } ?: return,
                "Взрослый добавил $amount монет. Это поддержка, а не оценка.",
            )
            return
        }
        apply { GameEngine.parentBonus(this, amount) }
    }

    suspend fun setSound(on: Boolean) {
        if (remoteEnabled) {
            applyRemoteState(runCatchingUser { api.updateProfile(SettingsIn(soundOn = on)) } ?: return)
            _state.value = _state.value.copy(soundOn = on)
            persist(_state.value)
            return
        }
        mutate { GameEngine.setSound(this, on) }
    }

    suspend fun setDemo(on: Boolean) = mutate { GameEngine.setDemo(this, on) }

    suspend fun setIncome(income: Int) {
        if (remoteEnabled) {
            applyRemoteState(runCatchingUser { api.updateProfile(SettingsIn(weeklyIncome = income)) } ?: return)
            return
        }
        apply { GameEngine.setIncome(this, income) }
    }

    suspend fun setEventMode(mode: String) {
        if (!remoteEnabled) return
        applyRemoteState(runCatchingUser { api.updateProfile(SettingsIn(eventMode = mode)) } ?: return)
        _state.value = _state.value.copy(eventMode = mode)
        persist(_state.value)
    }

    fun buyCheck(itemId: String): BuyCheck? {
        val item = _state.value.shop.firstOrNull { it.id == itemId } ?: return null
        if (remoteEnabled && !item.affordable) {
            return BuyCheck(false, "В статье «${item.category.label}» не хватает монет.")
        }
        return GameEngine.checkBuy(_state.value, item)
    }

    suspend fun resetProfile() {
        if (session.token() != null) {
            try {
                withContext(io) { api.reset() }
            } catch (_: Throwable) {
                // 404, если питомца на сервере ещё не было
            }
            remoteEnabled = true
        }
        session.clearPetCache()
        withContext(io) { db.stateDao().clear() }
        _state.value = SampleData.snapshot.copy(
            onboarded = false,
            ready = true,
            shop = catalog.shop,
            goals = catalog.goals,
            online = remoteEnabled,
        )
        _toasts.emit(ToastEvent("Профиль сброшен. Можно начать заново."))
    }

    suspend fun todayEvent(): TodayEvent? {
        val event = runCatchingUser { api.todayEvent() }?.toModel()
        if (event != null) {
            _state.value = _state.value.copy(todayEvent = event)
        }
        return event ?: _state.value.todayEvent
    }

    suspend fun chooseEvent(id: String, option: String): EventChoice? {
        val out = runCatchingUser { api.chooseEvent(id, ChooseIn(option)) } ?: return null
        applyRemoteState(out.state, out.note)
        refreshRemoteLists()
        return out.toModel()
    }

    suspend fun wordOfDay(): WordOfDay? {
        val word = runCatchingUser { api.wordOfDay() }?.toModel()
        if (word != null) _state.value = _state.value.copy(wordOfDay = word)
        return word
    }

    suspend fun diary(): DiaryEntry? = runCatchingUser { api.diary() }?.toModel()

    suspend fun dreamPlan(): DreamPlan? = runCatchingUser { api.dreamPlan() }?.toModel()

    suspend fun origin(): OriginStory? = runCatchingUser { api.origin() }?.toModel()

    suspend fun weekSummary(): String? = runCatchingUser { api.weekSummary() }?.text

    suspend fun aiQuiz(kind: String = "quiz"): AiQuiz? = runCatchingUser { api.quiz(kind) }?.toModel()

    suspend fun answerQuiz(index: Int, answerIndex: Int, kind: String = "quiz"): AiQuizAnswer? {
        val out = runCatchingUser { api.answerQuiz(QuizAnswerIn(index, answerIndex, kind)) } ?: return null
        if (out.coins > 0) runCatching { refreshRemoteLists() }
        return out.toModel()
    }

    suspend fun chat(text: String): ChatReply? = runCatchingUser { api.chat(ChatIn(text)) }?.toModel()

    private suspend fun refreshHomeExtras() {
        runCatching { withContext(io) { api.remark() } }.onSuccess { remark ->
            val mood = runCatching { PetMood.valueOf(remark.mood) }.getOrDefault(_state.value.pet.mood)
            val next = _state.value.copy(pet = _state.value.pet.copy(moodNote = remark.text, mood = mood))
            persist(next)
            _state.value = next
        }
        runCatching { withContext(io) { api.wordOfDay() } }.onSuccess {
            _state.value = _state.value.copy(wordOfDay = it.toModel())
        }
        runCatching { withContext(io) { api.todayEvent() } }.onSuccess { event ->
            _state.value = _state.value.copy(todayEvent = event?.toModel())
        }
    }

    private suspend fun refreshCatalogs() {
        runCatching { withContext(io) { api.goalCatalog() } }.onSuccess { remote ->
            if (remote.isEmpty()) return@onSuccess
            val mapped = remote.map { it.toModel() }
            val current = _state.value
            _state.value = current.copy(
                goals = mapped.map { goal ->
                    goal.copy(saved = current.goals.firstOrNull { it.id == goal.id || it.catalogSlug == goal.slugOrId() }?.saved ?: 0)
                },
                selectedGoalId = current.selectedGoalId.takeIf { id -> mapped.any { it.id == id } } ?: mapped.first().id,
            )
        }
        runCatching { withContext(io) { api.terms() } }.onSuccess { remote ->
            if (remote.isNotEmpty()) glossary = remote.map { it.toModel() }
        }
    }

    private fun dev.bober.finik.core.model.SavingsGoal.slugOrId(): String = catalogSlug.ifBlank { id }

    private suspend fun refreshShopAndTasks() {
        runCatching { withContext(io) { api.shopItems() } }.onSuccess { items ->
            if (items.isNotEmpty()) _state.value = _state.value.copy(shop = items.map { it.toModel() })
        }
        runCatching { withContext(io) { api.tasks() } }.onSuccess { items ->
            _state.value = _state.value.copy(tasks = items.map { it.toModel() })
        }
        persist(_state.value)
    }

    private suspend fun refreshRemoteLists() {
        refreshCatalogs()
        refreshShopAndTasks()
        runCatching { withContext(io) { api.badges() } }.onSuccess { items ->
            _state.value = _state.value.copy(badges = items.map { it.toModel() })
        }
        runCatching { withContext(io) { api.history() } }.onSuccess { history ->
            _state.value = _state.value.copy(
                history = history.toWeeks(),
                report = history.toReport() ?: _state.value.report,
            )
        }
        runCatching { withContext(io) { api.profile() } }.onSuccess { profile ->
            _state.value = _state.value.copy(
                earnedTotal = profile.earnedTotal,
                weeksDone = profile.weeksDone,
                soundOn = profile.soundOn,
                eventMode = profile.eventMode,
                vaccinatedUntil = profile.vaccinatedUntil,
            )
        }
        persist(_state.value)
    }

    private suspend fun ensureRemoteLogin(): Boolean {
        if (session.token() != null) return true
        return try {
            withContext(io) {
                val result = api.login(LoginIn(deviceIds.get()))
                session.saveToken(result.token)
                true
            }
        } catch (_: Throwable) {
            false
        }
    }

    private suspend fun applyRemoteState(out: StateOut, message: String? = null, warning: Boolean = false) {
        session.saveSnapshotJson(FinikJson.json.encodeToString(StateOut.serializer(), out))
        val next = out.toSnapshot(_state.value)
        persist(next)
        _state.value = next
        if (!message.isNullOrBlank()) _toasts.emit(ToastEvent(message, warning))
    }

    private suspend fun <T> runCatchingUser(block: suspend () -> T): T? {
        return try {
            withContext(io) { block() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            _toasts.emit(ToastEvent(e.userMessage(), warning = true))
            null
        }
    }

    private suspend fun apply(block: GameSnapshot.() -> EngineResult) {
        val result = _state.value.block()
        commit(result)
    }

    private suspend fun mutate(block: GameSnapshot.() -> GameSnapshot) {
        val next = _state.value.block()
        persist(next)
        _state.value = next
    }

    private suspend fun commit(result: EngineResult) {
        persist(result.state)
        _state.value = result.state
        if (result.message.isNotBlank()) {
            _toasts.emit(ToastEvent(result.message, result.warning))
        }
    }

    private suspend fun persist(snapshot: GameSnapshot) {
        if (!snapshot.onboarded) {
            withContext(io) { db.stateDao().clear() }
            return
        }
        withContext(io) {
            db.stateDao().upsert(StateEntity(json = snapshot.encode()))
        }
    }

    private fun today(): Long = LocalDate.now(ZoneOffset.UTC).toEpochDay()
}
