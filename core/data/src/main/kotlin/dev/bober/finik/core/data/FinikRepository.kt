package dev.bober.finik.core.data

import dev.bober.finik.core.data.content.ContentCatalog
import dev.bober.finik.core.data.game.GameEngine
import dev.bober.finik.core.data.snapshot.decodeSnapshot
import dev.bober.finik.core.data.snapshot.encode
import dev.bober.finik.core.database.FinikDatabase
import dev.bober.finik.core.database.StateEntity
import dev.bober.finik.core.model.EngineResult
import dev.bober.finik.core.model.GameSnapshot
import dev.bober.finik.core.model.PetPotStyle
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.core.model.SampleData
import dev.bober.finik.core.model.SpendCategory
import dev.bober.finik.core.model.TaskKind
import dev.bober.finik.core.model.TaskItem
import dev.bober.finik.core.model.TaskTarget
import dev.bober.finik.core.model.TaskTheme
import dev.bober.finik.core.network.ContentApi
import dev.bober.finik.core.network.RemoteContentDto
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

data class ToastEvent(val text: String, val warning: Boolean = false)

class FinikRepository(
    private val db: FinikDatabase,
    private val api: ContentApi,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) {
    private val scope = CoroutineScope(SupervisorJob() + io)
    private var catalog = ContentCatalog()

    private val _state = MutableStateFlow(SampleData.snapshot.copy(onboarded = false, ready = false))
    val state: StateFlow<GameSnapshot> = _state.asStateFlow()

    private val _toasts = MutableSharedFlow<ToastEvent>(extraBufferCapacity = 8)
    val toasts: SharedFlow<ToastEvent> = _toasts.asSharedFlow()

    val glossary get() = catalog.glossary
    val scenarios get() = catalog.scenarios
    val quiz get() = catalog.quiz

    init {
        scope.launch { boot() }
    }

    private suspend fun boot() {
        catalog = loadRemoteCatalog()
        val stored = db.stateDao().get()?.json
        val loaded = if (stored != null) {
            runCatching { decodeSnapshot(stored, catalog) }.getOrNull()
        } else {
            null
        }
        _state.value = (loaded ?: SampleData.snapshot.copy(onboarded = false, ready = true))
            .copy(shop = catalog.shop, ready = true)
        if (_state.value.onboarded) {
            apply { GameEngine.dailyOpen(this, today()) }
        }
    }

    suspend fun createProfile(
        name: String,
        species: PetSpecies,
        potStyle: PetPotStyle,
        income: Int,
    ) {
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

    suspend fun changePlan(category: SpendCategory, delta: Int) = apply { GameEngine.changePlan(this, category, delta) }
    suspend fun applyAdvice() = apply { GameEngine.applyAdvice(this) }
    suspend fun resetPlan() = apply { GameEngine.resetPlan(this) }
    suspend fun confirmPlan() = apply { GameEngine.confirmPlan(this) }
    suspend fun care(category: SpendCategory) = apply { GameEngine.care(this, category) }
    suspend fun buy(itemId: String) = apply { GameEngine.buy(this, itemId) }
    suspend fun deposit(amount: Int) = apply { GameEngine.deposit(this, amount) }
    suspend fun withdraw(amount: Int) = apply { GameEngine.withdraw(this, amount) }
    suspend fun selectGoal(id: String) = apply { GameEngine.selectGoal(this, id) }
    suspend fun completeTask(taskId: String, correct: Boolean, reward: Int) =
        apply { GameEngine.completeTask(this, taskId, correct, reward) }
    suspend fun closeWeek() = apply { GameEngine.closeWeek(this) }
    suspend fun repeatLastPlan() = apply { GameEngine.repeatLastPlan(this, report) }
    suspend fun parentBonus() = apply { GameEngine.parentBonus(this) }
    suspend fun setSound(on: Boolean) = mutate { GameEngine.setSound(this, on) }
    suspend fun setDemo(on: Boolean) = mutate { GameEngine.setDemo(this, on) }
    suspend fun setIncome(income: Int) = apply { GameEngine.setIncome(this, income) }

    fun buyCheck(itemId: String) = _state.value.shop.firstOrNull { it.id == itemId }?.let {
        GameEngine.checkBuy(_state.value, it)
    }

    suspend fun resetProfile() {
        withContext(io) { db.stateDao().clear() }
        _state.value = SampleData.snapshot.copy(onboarded = false, ready = true, shop = catalog.shop)
        _toasts.emit(ToastEvent("Профиль сброшен. Можно начать заново."))
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

    private suspend fun loadRemoteCatalog(): ContentCatalog {
        val remote: RemoteContentDto? = withContext(io) { api.fetch() }
        if (remote == null || remote.tasks.isEmpty()) return ContentCatalog()
        val extras = remote.tasks.map { dto ->
            TaskItem(
                id = dto.id,
                title = dto.title,
                subtitle = dto.subtitle.ifBlank { "с сервера" },
                kind = TaskKind.LESSON,
                reward = dto.reward,
                done = false,
                target = TaskTarget.SCENARIO,
                theme = TaskTheme.PLAN,
            )
        }
        val scenarios = remote.tasks
            .filter { it.choices.isNotEmpty() }
            .map { dto ->
                dev.bober.finik.core.model.ScenarioTask(
                    id = dto.id,
                    prompt = dto.prompt.ifBlank { dto.title },
                    choices = dto.choices.map {
                        dev.bober.finik.core.model.ScenarioChoice(it.label, it.correct, it.explanation)
                    },
                )
            }
        return ContentCatalog.withRemote(extras, scenarios)
    }

    private fun today(): Long = LocalDate.now(ZoneOffset.UTC).toEpochDay()
}
