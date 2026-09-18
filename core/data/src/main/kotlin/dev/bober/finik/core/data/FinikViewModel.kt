package dev.bober.finik.core.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bober.finik.core.model.PetPotStyle
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.core.model.SpendCategory
import kotlinx.coroutines.launch

/** Общий ViewModel: все вкладки читают один [FinikRepository]. */
class FinikViewModel(val repo: FinikRepository) : ViewModel() {
    val state = repo.state
    val toasts = repo.toasts

    fun createProfile(name: String, species: PetSpecies, pot: PetPotStyle, income: Int) =
        viewModelScope.launch { repo.createProfile(name, species, pot, income) }

    fun changePlan(category: SpendCategory, delta: Int) = viewModelScope.launch { repo.changePlan(category, delta) }
    fun applyAdvice() = viewModelScope.launch { repo.applyAdvice() }
    fun resetPlan() = viewModelScope.launch { repo.resetPlan() }
    fun confirmPlan() = viewModelScope.launch { repo.confirmPlan() }
    fun care(category: SpendCategory) = viewModelScope.launch { repo.care(category) }
    fun buy(itemId: String) = viewModelScope.launch { repo.buy(itemId) }
    fun deposit(amount: Int) = viewModelScope.launch { repo.deposit(amount) }
    fun withdraw(amount: Int) = viewModelScope.launch { repo.withdraw(amount) }
    fun selectGoal(id: String) = viewModelScope.launch { repo.selectGoal(id) }
    fun completeTask(id: String, correct: Boolean, reward: Int) =
        viewModelScope.launch { repo.completeTask(id, correct, reward) }
    fun closeWeek(after: () -> Unit = {}) = viewModelScope.launch {
        repo.closeWeek()
        after()
    }
    fun repeatLastPlan() = viewModelScope.launch { repo.repeatLastPlan() }
    fun parentBonus() = viewModelScope.launch { repo.parentBonus() }
    fun setSound(on: Boolean) = viewModelScope.launch { repo.setSound(on) }
    fun setDemo(on: Boolean) = viewModelScope.launch { repo.setDemo(on) }
    fun setIncome(income: Int) = viewModelScope.launch { repo.setIncome(income) }
    fun resetProfile() = viewModelScope.launch { repo.resetProfile() }
}
