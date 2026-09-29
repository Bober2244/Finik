package dev.bober.finik.core.model

data class AdventureChoice(val id: String, val title: String, val detail: String, val cost: Int)
data class AdventureScene(
    val kind: String, val title: String, val story: String,
    val foodNeed: Int = 0, val waterNeed: Int = 0, val reserveHint: Int = 0,
    val choices: List<AdventureChoice> = emptyList(),
)
data class Adventure(
    val slug: String, val title: String, val summary: String, val stage: Int, val total: Int,
    val wallet: Int, val reserve: Int, val care: Int, val joy: Int, val score: Int, val dream: Int,
    val feedback: String, val petReaction: String, val completed: Boolean, val rewarded: Boolean,
    val medal: String?, val outcome: String, val scene: AdventureScene,
)
data class AdventureAction(
    val expectedStage: Int, val food: Int? = null, val water: Int? = null,
    val reserve: Int? = null, val choiceId: String? = null, val amount: Int? = null,
)
data class LearningReview(
    val topic: String?, val title: String, val question: String,
    val options: List<String>, val nextDue: String?,
)
data class ReviewAnswer(val correct: Boolean, val explanation: String, val nextDue: String?)
