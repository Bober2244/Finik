package dev.bober.finik.core.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bober.finik.core.model.*
import dev.bober.finik.core.model.PetAppearance
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.core.model.AiQuiz
import dev.bober.finik.core.model.SpendCategory
import kotlinx.coroutines.launch

/** Общий ViewModel: все вкладки читают один [FinikRepository]. */
class FinikViewModel(val repo: FinikRepository) : ViewModel() {
    val busy = repo.busy
    val serverAddress = repo.serverAddress
    val canChangeServer = repo.canChangeServer
    val error = repo.error
    val state = repo.state
    val loadProblem = repo.loadProblem
    val toasts = repo.toasts
    val careEvents = repo.careEvents
    val extrasEnabled = repo.extrasEnabled

    fun connectToServer(rawAddress: String, onResult: (Boolean, String) -> Unit) = viewModelScope.launch {
        val (success, message) = repo.connectToServer(rawAddress)
        onResult(success, message)
    }

    fun createProfile(name: String, species: PetSpecies, appearance: PetAppearance, income: Int) =
        viewModelScope.launch { repo.createProfile(name, species, appearance, income) }

    fun changePlan(category: SpendCategory, delta: Int) = viewModelScope.launch { repo.changePlan(category, delta) }
    fun applyAdvice() = viewModelScope.launch { repo.applyAdvice() }
    fun resetPlan() = viewModelScope.launch { repo.resetPlan() }
    fun confirmPlan() = viewModelScope.launch { repo.confirmPlan() }
    fun care(category: SpendCategory) = viewModelScope.launch { repo.care(category) }
    fun buy(itemId: String) = viewModelScope.launch { repo.buy(itemId) }
    fun customize(pot: String? = null, accessory: String? = null, lookVariant: Int? = null) =
        viewModelScope.launch { repo.customize(pot, accessory, lookVariant) }
    fun customizeAppearance(appearance: PetAppearance) = viewModelScope.launch { repo.customizeAppearance(appearance) }
    fun deposit(amount: Int) = viewModelScope.launch { repo.deposit(amount) }
    fun withdraw(amount: Int) = viewModelScope.launch { repo.withdraw(amount) }
    fun selectGoal(id: String) = viewModelScope.launch { repo.selectGoal(id) }
    fun closeWeek(after: () -> Unit = {}) = viewModelScope.launch {
        if (repo.closeWeek()) after()
    }
    fun repeatLastPlan() = viewModelScope.launch { repo.repeatLastPlan() }
    fun parentBonus(pin: String = "", amount: Int = 10, reason: String = "Поддержка родителя") = viewModelScope.launch { repo.parentBonus(amount, reason, pin) }
    fun setSound(on: Boolean) = viewModelScope.launch { repo.setSound(on) }
    fun setMotion(on: Boolean) = viewModelScope.launch { repo.setMotion(on) }
    fun setIncome(income: Int) = viewModelScope.launch { repo.setIncome(income) }
    fun resetProfile(after: () -> Unit = {}) = viewModelScope.launch {
        if (repo.resetProfile()) after()
    }
    fun retryLoad() = viewModelScope.launch { repo.retryLoad() }
    fun setOnlineExtras(enabled: Boolean) = viewModelScope.launch { repo.setOnlineExtras(enabled) }
    fun chooseEvent(id: String, option: String) = viewModelScope.launch { repo.chooseEvent(id, option) }
    fun loadExtra(kind: String, onText: (String) -> Unit) = viewModelScope.launch {
        val result = when (kind) {
            "diary" -> repo.diary()?.text
            "dream" -> repo.dreamPlan()?.advice
            "origin" -> repo.origin()?.text
            "summary" -> repo.weekSummary()
            else -> null
        }
        onText(result?.takeIf { it.isNotBlank() } ?: "Материал сейчас недоступен.")
    }
    fun loadAiQuiz(onQuiz: (AiQuiz?) -> Unit) = viewModelScope.launch { onQuiz(repo.aiQuiz()) }
    fun answerAiQuiz(index: Int, answerIndex: Int, onText: (String) -> Unit) = viewModelScope.launch {
        onText(repo.answerQuiz(index, answerIndex)?.explanation ?: "Ответ сейчас недоступен.")
    }
    fun chat(text: String, onReply: (String) -> Unit = {}) = viewModelScope.launch {
        onReply(repo.chat(text)?.text ?: "Ответ сейчас недоступен.")
    }

    fun refresh() = viewModelScope.launch { repo.refresh() }
    fun startDemo(prepared: Boolean) = viewModelScope.launch { repo.startDemo(prepared) }
    fun leaveDemo() = viewModelScope.launch { repo.leaveDemo() }
    fun advanceDemoDay() = viewModelScope.launch { repo.advanceDemoDay() }
    fun claimTask(slug: String) = viewModelScope.launch { repo.claimTask(slug) }
    fun loadTaskQuestions(slug: String, onQuestions: (List<QuizQuestion>) -> Unit) =
        viewModelScope.launch { onQuestions(repo.taskQuestions(slug)) }
    fun answerTask(slug: String, questionSlug: String, answerIndex: Int? = null, answerValue: Int? = null, onFeedback: (LessonAnswer?) -> Unit) =
        viewModelScope.launch { onFeedback(repo.answerTask(slug, questionSlug, answerIndex, answerValue)) }
    fun taskHint(slug: String, questionSlug: String, onText: (String?) -> Unit) =
        viewModelScope.launch { onText(repo.taskHint(slug, questionSlug)) }
    fun loadAdventure(slug: String, onAdventure: (Adventure?) -> Unit) =
        viewModelScope.launch { onAdventure(repo.adventure(slug)) }
    fun playAdventure(slug: String, action: AdventureAction, onAdventure: (Adventure?) -> Unit) =
        viewModelScope.launch { onAdventure(repo.playAdventure(slug, action)) }
    fun restartAdventure(slug: String, onAdventure: (Adventure?) -> Unit) =
        viewModelScope.launch { onAdventure(repo.restartAdventure(slug)) }
    fun loadReview(onReview: (LearningReview?) -> Unit) =
        viewModelScope.launch { onReview(repo.review()) }
    fun answerReview(topic: String, index: Int, onAnswer: (ReviewAnswer?) -> Unit) =
        viewModelScope.launch { onAnswer(repo.answerReview(topic, index)) }
}
