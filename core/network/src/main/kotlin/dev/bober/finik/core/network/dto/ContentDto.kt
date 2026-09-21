package dev.bober.finik.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class GoalCatalogOut(
    val slug: String,
    val title: String,
    val target: Int = 0,
    val why: String = "",
)

@Serializable
data class TermOut(
    val word: String,
    val meaning: String = "",
    val example: String = "",
)
