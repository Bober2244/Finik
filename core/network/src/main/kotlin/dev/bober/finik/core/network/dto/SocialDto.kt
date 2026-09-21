package dev.bober.finik.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginIn(
    val deviceId: String,
)

@Serializable
data class LoginOut(
    val token: String,
    val hasPet: Boolean = false,
)

@Serializable
data class ProfileOut(
    val earnedTotal: Int = 0,
    val savedTotal: Int = 0,
    val weeksDone: Int = 0,
    val tasksDone: Int = 0,
    val weeklyIncome: Int = 40,
    val soundOn: Boolean = true,
    val eventMode: String = "random",
    val vaccinatedUntil: Int = 0,
    val incomeOptions: List<Int> = listOf(20, 40, 60),
    val state: StateOut? = null,
)

@Serializable
data class SettingsIn(
    val weeklyIncome: Int? = null,
    val soundOn: Boolean? = null,
    val eventMode: String? = null,
)

@Serializable
data class BadgeOut(
    val slug: String,
    val name: String,
    val note: String = "",
    val percent: Int = 0,
    val isDone: Boolean = false,
)

@Serializable
data class BonusIn(
    val amount: Int,
    val reason: String,
    val pin: String,
)

@Serializable
data class BonusOut(
    val amount: Int = 0,
    val reason: String = "",
    val state: StateOut,
)
