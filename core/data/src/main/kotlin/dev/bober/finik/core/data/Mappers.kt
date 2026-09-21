package dev.bober.finik.core.data

import dev.bober.finik.core.model.AiQuiz
import dev.bober.finik.core.model.AiQuizAnswer
import dev.bober.finik.core.model.AiQuizQuestion
import dev.bober.finik.core.model.Badge
import dev.bober.finik.core.model.CareAction
import dev.bober.finik.core.model.ChatReply
import dev.bober.finik.core.model.DiaryEntry
import dev.bober.finik.core.model.DreamPlan
import dev.bober.finik.core.model.DreamStep
import dev.bober.finik.core.model.EventChoice
import dev.bober.finik.core.model.EventOption
import dev.bober.finik.core.model.GameSnapshot
import dev.bober.finik.core.model.GlossaryTerm
import dev.bober.finik.core.model.HistoryTone
import dev.bober.finik.core.model.HistoryWeek
import dev.bober.finik.core.model.ItemGlyph
import dev.bober.finik.core.model.LessonAnswer
import dev.bober.finik.core.model.NeedLevel
import dev.bober.finik.core.model.OriginStory
import dev.bober.finik.core.model.PetMood
import dev.bober.finik.core.model.PetPotStyle
import dev.bober.finik.core.model.PetProfile
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.core.model.PlanEntry
import dev.bober.finik.core.model.QuizQuestion
import dev.bober.finik.core.model.ReportRow
import dev.bober.finik.core.model.SavingsGoal
import dev.bober.finik.core.model.ShopItem
import dev.bober.finik.core.model.SpendCategory
import dev.bober.finik.core.model.SpendKind
import dev.bober.finik.core.model.StreakDay
import dev.bober.finik.core.model.TaskItem
import dev.bober.finik.core.model.TaskKind
import dev.bober.finik.core.model.TaskTarget
import dev.bober.finik.core.model.TaskTheme
import dev.bober.finik.core.model.TodayEvent
import dev.bober.finik.core.model.WeekPlan
import dev.bober.finik.core.model.WeekReport
import dev.bober.finik.core.model.WordOfDay
import dev.bober.finik.core.model.xpPercentInStage
import dev.bober.finik.core.network.dto.AnswerOut
import dev.bober.finik.core.network.dto.BadgeOut
import dev.bober.finik.core.network.dto.ChatOut
import dev.bober.finik.core.network.dto.ChoiceOut
import dev.bober.finik.core.network.dto.DiaryOut
import dev.bober.finik.core.network.dto.DreamPlanOut
import dev.bober.finik.core.network.dto.EventOut
import dev.bober.finik.core.network.dto.GoalCatalogOut
import dev.bober.finik.core.network.dto.GoalOut
import dev.bober.finik.core.network.dto.HistoryOut
import dev.bober.finik.core.network.dto.OriginOut
import dev.bober.finik.core.network.dto.PetOut
import dev.bober.finik.core.network.dto.PlanIn
import dev.bober.finik.core.network.dto.QuestionOut
import dev.bober.finik.core.network.dto.QuizAnswerOut
import dev.bober.finik.core.network.dto.QuizOut
import dev.bober.finik.core.network.dto.ShopItemOut
import dev.bober.finik.core.network.dto.StateOut
import dev.bober.finik.core.network.dto.TaskOut
import dev.bober.finik.core.network.dto.TermOut
import dev.bober.finik.core.network.dto.WeekOut
import dev.bober.finik.core.network.dto.WordOfDayOut
import kotlin.math.max

internal fun String.toCategoryOrNull(): SpendCategory? =
    runCatching { SpendCategory.valueOf(this) }.getOrNull()

internal fun String.toCategory(): SpendCategory = toCategoryOrNull() ?: SpendCategory.FOOD

internal fun PetOut.toModel(): PetProfile {
    val look = lookVariant.coerceIn(0, PetPotStyle.entries.lastIndex)
    return PetProfile(
        name = name,
        species = runCatching { PetSpecies.valueOf(species) }.getOrDefault(PetSpecies.FINIK),
        potStyle = PetPotStyle.entries[look],
        stageIndex = stageIndex,
        xp = xpPercentInStage(xp),
        totalXp = xp,
        mood = runCatching { PetMood.valueOf(mood) }.getOrDefault(PetMood.OKAY),
        moodNote = moodNote,
        dayOfWeek = 1,
        lookVariant = look,
        equippedPot = equippedPot,
        equippedAccessory = equippedAccessory,
    )
}

internal fun WeekOut.toPlan(freeCoins: Int, weeklyIncome: Int): WeekPlan = WeekPlan(
    entries = SpendCategory.entries.map { category ->
        val row = entries.firstOrNull { it.category == category.name }
        PlanEntry(
            category = category,
            planned = row?.planned ?: 0,
            spent = row?.spent ?: 0,
        )
    },
    freeCoins = freeCoins,
    weeklyIncome = weeklyIncome,
)

internal fun GoalOut.toModel(): SavingsGoal {
    val slug = catalogSlug.ifBlank { id }
    return SavingsGoal(
        id = slug,
        title = title,
        target = target,
        saved = saved,
        why = why,
        catalogSlug = slug,
    )
}

internal fun GoalCatalogOut.toModel(): SavingsGoal = SavingsGoal(
    id = slug,
    title = title,
    target = target,
    saved = 0,
    why = why,
    catalogSlug = slug,
)

internal fun ShopItemOut.toModel(): ShopItem {
    val category = this.category.toCategory()
    val kind = if (this.kind == "WANT") SpendKind.OPTIONAL else SpendKind.REQUIRED
    val effect = buildString {
        if (restore > 0) append("Восстанавливает $restore.")
        if (xpBonus > 0) {
            if (isNotEmpty()) append(' ')
            append("+$xpBonus опыта.")
        }
    }
    return ShopItem(
        id = slug,
        name = name,
        cost = cost,
        oldCost = oldCost,
        category = category,
        glyph = runCatching { ItemGlyph.valueOf(glyph) }.getOrDefault(ItemGlyph.CIRCLE),
        kind = kind,
        effect = effect,
        restore = restore,
        xpBonus = xpBonus,
        slot = slot,
        owned = owned,
        equipped = equipped,
        leftInCategory = leftInCategory,
        affordable = affordable,
    )
}

internal fun TaskOut.toModel(): TaskItem {
    val kind = runCatching { TaskKind.valueOf(this.kind) }.getOrDefault(TaskKind.LESSON)
    val target = this.target?.let { runCatching { TaskTarget.valueOf(it) }.getOrNull() }
    val theme = when (target) {
        TaskTarget.GOAL -> TaskTheme.SAVE
        TaskTarget.SHOP -> TaskTheme.BUY
        else -> TaskTheme.PLAN
    }
    return TaskItem(
        id = slug,
        title = title,
        subtitle = subtitle,
        kind = kind,
        reward = reward,
        done = done,
        target = target,
        theme = theme,
        progress = progress,
        goalCount = goal,
        rewarded = rewarded,
    )
}

internal fun QuestionOut.toModel(): QuizQuestion = QuizQuestion(
    question = question,
    options = options,
    rightIndex = -1,
    explanation = "",
)

internal fun AnswerOut.toModel(): LessonAnswer = LessonAnswer(
    correct = correct,
    explanation = explanation,
    lessonDone = lessonDone,
    answered = answered,
    total = total,
)

internal fun HistoryOut.toWeeks(): List<HistoryWeek> = weeks.map { week ->
    HistoryWeek(
        label = "н${week.number}",
        value = week.saved,
        tone = runCatching { HistoryTone.valueOf(week.tone) }.getOrDefault(HistoryTone.MID),
        barHeight = (week.saved * 4).coerceIn(8, 70),
    )
}

internal fun HistoryOut.toReport(): WeekReport? {
    if (lastReport.isEmpty() && lastSummary.isBlank()) return null
    return WeekReport(
        week = weeks.maxOfOrNull { it.number } ?: 0,
        rows = lastReport.mapNotNull { row ->
            row.category.toCategoryOrNull()?.let { ReportRow(it, row.planned, row.actual) }
        },
        summary = lastSummary,
        note = lastStory,
    )
}

internal fun BadgeOut.toModel(): Badge = Badge(
    name = name,
    note = note,
    percent = if (isDone) max(percent, 100) else percent,
    slug = slug,
)

internal fun TermOut.toModel(): GlossaryTerm = GlossaryTerm(
    term = word,
    meaning = meaning,
    example = example,
)

internal fun EventOut.toModel(): TodayEvent = TodayEvent(
    id = id,
    slug = slug,
    title = title,
    text = text,
    day = day,
    options = options.map { EventOption(it.key, it.label) },
    chosen = chosen,
)

internal fun ChoiceOut.toModel(): EventChoice = EventChoice(
    note = note,
    coinsDelta = coinsDelta,
    xpDelta = xpDelta,
    needChanges = needChanges.mapNotNull { (key, value) ->
        key.toCategoryOrNull()?.let { it to value }
    }.toMap(),
    spent = spent.mapNotNull { (key, value) ->
        key.toCategoryOrNull()?.let { it to value }
    }.toMap(),
)

internal fun WordOfDayOut.toModel(): WordOfDay = WordOfDay(word, meaning, example, source)

internal fun DreamPlanOut.toModel(): DreamPlan = DreamPlan(
    title = title,
    remain = remain,
    weeklySave = weeklySave,
    weeksLeft = weeksLeft,
    steps = steps.map { DreamStep(it.title, it.coins) },
    advice = advice,
    source = source,
    cutPlay = cutPlay,
    fasterSave = fasterSave,
    weeksSaved = weeksSaved,
)

internal fun OriginOut.toModel(): OriginStory = OriginStory(text, source)

internal fun DiaryOut.toModel(): DiaryEntry = DiaryEntry(text, source, weekNumber)

internal fun QuizOut.toModel(): AiQuiz = AiQuiz(
    kind = kind,
    source = source,
    rewarded = rewarded,
    questions = questions.map { AiQuizQuestion(it.index, it.question, it.options) },
)

internal fun QuizAnswerOut.toModel(): AiQuizAnswer =
    AiQuizAnswer(correct, explanation, coins, rewarded)

internal fun ChatOut.toModel(): ChatReply = ChatReply(text, source, blocked, chatLeft)

internal fun WeekPlan.toPlanIn(): PlanIn = PlanIn(
    food = entry(SpendCategory.FOOD).planned,
    water = entry(SpendCategory.WATER).planned,
    play = entry(SpendCategory.PLAY).planned,
    save = entry(SpendCategory.SAVE).planned,
)

internal fun PetPotStyle.toLookVariant(): Int = ordinal.coerceIn(0, 2)

fun StateOut.toSnapshot(previous: GameSnapshot): GameSnapshot {
    val selected = goal.toModel()
    val previousGoals = previous.goals
    val goals = when {
        previousGoals.any { it.id == selected.id || it.catalogSlug == selected.catalogSlug } ->
            previousGoals.map { current ->
                if (current.id == selected.id || current.catalogSlug == selected.catalogSlug) {
                    selected.copy(id = current.id, catalogSlug = selected.catalogSlug.ifBlank { current.catalogSlug })
                } else {
                    current
                }
            }
        else -> listOf(selected) + previousGoals
    }
    val pet = pet.toModel().copy(dayOfWeek = week.day)
    val needs = this.pet.needs.mapNotNull { need ->
        need.category.toCategoryOrNull()?.let { NeedLevel(it, need.percent) }
    }.ifEmpty { previous.needs }
    val care = careActions.mapNotNull { action ->
        action.category.toCategoryOrNull()?.let { CareAction(action.label, it, action.cost) }
    }.ifEmpty { previous.care }
    val streak = streak.days.map { StreakDay(it.label, it.done) }.ifEmpty { previous.streak }
    return previous.copy(
        onboarded = true,
        ready = true,
        online = true,
        pet = pet,
        plan = week.toPlan(freeCoins, weeklyIncome),
        planConfirmed = week.planConfirmed,
        needs = needs,
        care = care,
        streak = streak,
        goals = goals,
        selectedGoalId = selected.id,
        ownedCosmetics = ownedCosmetics,
        lastIncome = lastIncome,
        lastIncomeNote = lastIncomeNote,
        lastPurchaseNote = lastPurchaseNote,
        lastPurchaseAmount = lastPurchaseAmount,
    )
}
