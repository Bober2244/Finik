package dev.bober.finik.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class AdventureChoiceOut(val id: String, val title: String, val detail: String = "", val cost: Int = 0)
@Serializable
data class AdventureSceneOut(
    val kind: String, val title: String, val story: String,
    val foodNeed: Int = 0, val waterNeed: Int = 0, val reserveHint: Int = 0,
    val choices: List<AdventureChoiceOut> = emptyList(),
)
@Serializable
data class AdventureOut(
    val slug: String, val title: String, val summary: String, val stage: Int, val total: Int,
    val wallet: Int, val reserve: Int, val care: Int, val joy: Int, val score: Int, val dream: Int,
    val feedback: String, val petReaction: String = "", val completed: Boolean, val rewarded: Boolean,
    val medal: String? = null, val outcome: String = "", val scene: AdventureSceneOut,
)
@Serializable
data class AdventureActionIn(
    val expectedStage: Int, val food: Int? = null, val water: Int? = null,
    val reserve: Int? = null, val choiceId: String? = null, val amount: Int? = null,
)
@Serializable
data class ReviewOut(
    val topic: String? = null, val title: String, val question: String,
    val options: List<String> = emptyList(), val nextDue: String? = null,
)
@Serializable
data class ReviewAnswerIn(val topic: String, val answerIndex: Int)
@Serializable
data class ReviewAnswerOut(val correct: Boolean, val explanation: String, val nextDue: String? = null)
@Serializable
data class HintIn(val questionSlug: String)
@Serializable
data class HintOut(val text: String, val source: String = "fallback")
