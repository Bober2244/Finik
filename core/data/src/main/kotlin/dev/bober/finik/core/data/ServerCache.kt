package dev.bober.finik.core.data

import dev.bober.finik.core.model.GameSnapshot
import dev.bober.finik.core.network.dto.*
import kotlinx.serialization.Serializable

/** Exact server responses: no local economy is executed when reading the offline cache. */
@Serializable
internal data class ServerCache(
    val schema: Int = 1,
    val serverAddress: String = "",
    val state: StateOut,
    val shop: List<ShopItemOut> = emptyList(),
    val tasks: List<TaskOut> = emptyList(),
    val profile: ProfileOut? = null,
    val history: HistoryOut = HistoryOut(),
    val badges: List<BadgeOut> = emptyList(),
    val terms: List<TermOut> = emptyList(),
    val event: EventOut? = null,
    val word: WordOfDayOut? = null,
    val cachedAt: String,
    val motionOn: Boolean = true,
) {
    fun toSnapshot(base: GameSnapshot, online: Boolean): GameSnapshot = state.toSnapshot(base).copy(
        online = online,
        shop = shop.map { it.toModel() },
        tasks = tasks.map { it.toModel() },
        history = history.toWeeks(),
        report = history.toReport(),
        badges = badges.map { it.toModel() },
        earnedTotal = profile?.earnedTotal ?: 0,
        weeksDone = profile?.weeksDone ?: (state.week.number - 1),
        soundOn = profile?.soundOn ?: true,
        eventMode = profile?.eventMode ?: "random",
        vaccinatedUntil = profile?.vaccinatedUntil ?: 0,
        nextWeekIncome = profile?.weeklyIncome ?: state.weeklyIncome,
        todayEvent = event?.toModel(),
        wordOfDay = word?.toModel(),
        cachedAt = cachedAt,
        motionOn = motionOn,
        transactions = emptyList(),
        weekLog = emptyList(),
    )
}
