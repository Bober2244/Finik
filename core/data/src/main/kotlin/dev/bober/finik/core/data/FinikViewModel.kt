package dev.bober.finik.core.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bober.finik.core.model.PetAppearance
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.core.model.AiQuiz
import dev.bober.finik.core.model.SpendCategory
import kotlinx.coroutines.launch

/** Общий ViewModel: все вкладки читают один [FinikRepository]. */
class FinikViewModel(val repo: FinikRepository) : ViewModel() {
    val state = repo.state
    val loadProblem = repo.loadProblem
    val toasts = repo.toasts
    val careEvents = repo.careEvents
    val extrasEnabled = repo.extrasEnabled

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
    fun completeTask(id: String, correct: Boolean, reward: Int) =
        viewModelScope.launch { repo.completeTask(id, correct, reward) }
    fun closeWeek(after: () -> Unit = {}) = viewModelScope.launch {
        if (repo.closeWeek()) after()
    }
    fun repeatLastPlan() = viewModelScope.launch { repo.repeatLastPlan() }
    fun parentBonus(pin: String = "") = viewModelScope.launch { repo.parentBonus(pin = pin) }
    fun setSound(on: Boolean) = viewModelScope.launch { repo.setSound(on) }
    fun setMotion(on: Boolean) = viewModelScope.launch { repo.setMotion(on) }
    fun setDemo(on: Boolean) = viewModelScope.launch { repo.setDemo(on) }
    fun setIncome(income: Int) = viewModelScope.launch { repo.setIncome(income) }
    fun resetProfile(localOnly: Boolean = false, after: () -> Unit = {}) = viewModelScope.launch {
        if (repo.resetProfile(localOnly)) after()
    }
    fun retryLoad() = viewModelScope.launch { repo.retryLoad() }
    fun setOnlineExtras(enabled: Boolean) = viewModelScope.launch { repo.setOnlineExtras(enabled) }
    fun chooseEvent(id: String, option: String) = viewModelScope.launch { repo.chooseEvent(id, option) }
    fun loadExtra(kind: String, onText: (String) -> Unit) = viewModelScope.launch {
        val result = when (kind) {
            "diary" -> repo.diary()?.text
            "dream" -> repo.dreamPlan()?.advice
            "origin" -> repo.origin()?.text
            "summary" -> state.value.report?.let { "${it.summary}\n${it.note}" }
            else -> null
        }
        onText(result?.takeIf { it.isNotBlank() } ?: "Материал сейчас недоступен.")
    }
    fun loadAiQuiz(onQuiz: (AiQuiz?) -> Unit) = viewModelScope.launch { onQuiz(repo.aiQuiz()) }
    fun answerAiQuiz(index: Int, answerIndex: Int, onText: (String) -> Unit) = viewModelScope.launch {
        onText(repo.answerQuiz(index, answerIndex)?.explanation ?: "Ответ сейчас недоступен.")
    }
    fun chat(text: String, onReply: (String) -> Unit = {}) = viewModelScope.launch {
        repo.chat(text)?.let { onReply(it.text) }
    }
}
