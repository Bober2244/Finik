package dev.bober.finik.core.data.game

import dev.bober.finik.core.model.Badge
import dev.bober.finik.core.model.BuyCheck
import dev.bober.finik.core.model.CareAction
import dev.bober.finik.core.model.EngineResult
import dev.bober.finik.core.model.GameSnapshot
import dev.bober.finik.core.model.HistoryTone
import dev.bober.finik.core.model.HistoryWeek
import dev.bober.finik.core.model.LogTone
import dev.bober.finik.core.model.NeedLevel
import dev.bober.finik.core.model.PetAppearance
import dev.bober.finik.core.model.PetMood
import dev.bober.finik.core.model.PetPotStyle
import dev.bober.finik.core.model.PetProfile
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.core.model.PlanEntry
import dev.bober.finik.core.model.ReportRow
import dev.bober.finik.core.model.SavingsGoal
import dev.bober.finik.core.model.ShopItem
import dev.bober.finik.core.model.SpendCategory
import dev.bober.finik.core.model.SpendKind
import dev.bober.finik.core.model.StreakDay
import dev.bober.finik.core.model.TaskItem
import dev.bober.finik.core.model.WeekLogEntry
import dev.bober.finik.core.model.WeekPlan
import dev.bober.finik.core.model.WeekReport
import dev.bober.finik.core.model.growthStages
import dev.bober.finik.core.model.stageIndexFor
import dev.bober.finik.core.model.xpPercentInStage
import kotlin.math.ceil
import kotlin.math.roundToInt

/** Правила игровой экономики. Чистые функции — покрываются юнит-тестами. */
object GameEngine {

    val careActions: List<CareAction> = listOf(
        CareAction(label = "Напоить", category = SpendCategory.WATER, cost = 2),
        CareAction(label = "Покормить", category = SpendCategory.FOOD, cost = 3),
        CareAction(label = "Поиграть", category = SpendCategory.PLAY, cost = 2),
    )

    fun createProfile(
        name: String,
        species: PetSpecies,
        potStyle: PetPotStyle,
        income: Int,
        shop: List<ShopItem>,
        goals: List<SavingsGoal>,
        tasks: List<TaskItem>,
        epochDay: Long,
        appearance: PetAppearance = PetAppearance.fromLegacy(potStyle.ordinal),
    ): GameSnapshot {
        val petName = name.trim().ifBlank { species.title }
        val pet = refreshPet(
            PetProfile(
                name = petName,
                species = species,
                potStyle = potStyle,
                appearance = appearance,
                stageIndex = 0,
                xp = 0,
                totalXp = 0,
                mood = PetMood.HAPPY,
                moodNote = "Новая неделя. Сначала разложи монеты: нужное, желаемое, копилка.",
                dayOfWeek = 1,
            ),
            needs = starterNeeds(),
        )
        return GameSnapshot(
            onboarded = true,
            pet = pet,
            plan = emptyPlan(income),
            planConfirmed = false,
            needs = starterNeeds(),
            care = careActions,
            streak = streakFromMask(1),
            tasks = tasks.map { it.copy(done = false) },
            shop = shop,
            goals = goals.map { it.copy(saved = 0) },
            selectedGoalId = goals.first().id,
            history = emptyList(),
            weekLog = emptyList(),
            badges = computeBadges(0, 0, 0, 0, 0, 0),
            report = null,
            demoMode = true,
            soundOn = true,
            earnedTotal = income,
            weeksDone = 0,
            lastOpenEpochDay = epochDay,
        ).withBadges()
    }

    fun changePlan(state: GameSnapshot, category: SpendCategory, delta: Int): EngineResult {
        if (state.planConfirmed) {
            return EngineResult(state, "План уже подтверждён. Менять статьи можно со следующей недели.", warning = true)
        }
        val entry = state.plan.entry(category)
        val planned = (entry.planned + delta).coerceAtLeast(0)
        val spent = entry.spent
        if (planned < spent) {
            return EngineResult(state, "Нельзя поставить меньше, чем уже потрачено.", warning = true)
        }
        val others = state.plan.entries.filter { it.category != category }.sumOf { it.planned }
        if (others + planned > state.plan.weeklyIncome) {
            return EngineResult(state, "Так не получится: всего ${state.plan.weeklyIncome} монет на неделю.", warning = true)
        }
        val entries = state.plan.entries.map {
            if (it.category == category) it.copy(planned = planned) else it
        }
        val allocated = entries.sumOf { it.planned }
        val next = state.copy(
            plan = state.plan.copy(entries = entries, freeCoins = state.plan.weeklyIncome - allocated),
        )
        return EngineResult(next, "В «${category.label}» теперь $planned. Свободно ${next.plan.freeCoins}.")
    }

    fun applyAdvice(state: GameSnapshot): EngineResult {
        if (state.planConfirmed) {
            return EngineResult(state, "План уже подтверждён.", warning = true)
        }
        val income = state.plan.weeklyIncome
        val food = (income * 0.40).roundToInt()
        val play = (income * 0.30).roundToInt()
        val water = (income * 0.10).roundToInt()
        val save = income - food - play - water
        val entries = listOf(
            PlanEntry(SpendCategory.FOOD, food, 0),
            PlanEntry(SpendCategory.WATER, water, 0),
            PlanEntry(SpendCategory.PLAY, play, 0),
            PlanEntry(SpendCategory.SAVE, save, 0),
        )
        val next = state.copy(plan = state.plan.copy(entries = entries, freeCoins = 0))
        return EngineResult(next, "Совет 40/30/10/20: еда $food, игры $play, вода $water, копилка $save.")
    }

    fun resetPlan(state: GameSnapshot): EngineResult {
        if (state.planConfirmed) {
            return EngineResult(state, "После подтверждения план не сбрасывается.", warning = true)
        }
        val next = state.copy(plan = emptyPlan(state.plan.weeklyIncome))
        return EngineResult(next, "План очищен. Разложи монеты заново.")
    }

    fun confirmPlan(state: GameSnapshot): EngineResult {
        if (state.planConfirmed) return EngineResult(state, "План уже собран.")
        if (state.plan.freeCoins > 0) {
            return EngineResult(
                state,
                "Осталось ${state.plan.freeCoins} свободно. Разложи всё: нужное, желаемое и копилку.",
                warning = true,
            )
        }
        val savePercent = state.plan.percentOf(SpendCategory.SAVE)
        val note = if (savePercent < 20) {
            "План собран, но в копилке меньше 20%. Цель будет дальше."
        } else {
            "План собран. Теперь траты идут только из статей."
        }
        val next = refreshMood(state.copy(planConfirmed = true, pet = state.pet.copy(moodNote = note)))
        return EngineResult(next, note, warning = savePercent < 20)
    }

    fun checkBuy(state: GameSnapshot, item: ShopItem): BuyCheck {
        if (!state.planConfirmed) {
            return BuyCheck(false, "Сначала подтверди план недели. Иначе непонятно, из какой статьи тратить.")
        }
        val left = state.plan.entry(item.category).left
        if (left >= item.cost) return BuyCheck(true, "")
        val need = item.cost - left
        val hint = if (item.kind == SpendKind.OPTIONAL) {
            "Не хватает $need в статье «${item.category.label}». Это желаемое: можно подождать, выполнить задание или поменять план на следующей неделе."
        } else {
            "Не хватает $need в статье «${item.category.label}». Нужное лучше не пропускать: заработай монеты в заданиях или урежь желаемое на следующей неделе."
        }
        return BuyCheck(false, hint)
    }

    fun buy(state: GameSnapshot, itemId: String): EngineResult {
        val item = state.shop.firstOrNull { it.id == itemId }
            ?: return EngineResult(state, "Такого товара нет.", warning = true)
        val check = checkBuy(state, item)
        if (!check.allowed) return EngineResult(state, check.message, warning = true)
        val entries = state.plan.entries.map {
            if (it.category == item.category) it.copy(spent = it.spent + item.cost) else it
        }
        val needs = bumpNeed(state.needs, item.category, if (item.kind == SpendKind.REQUIRED) 18 else 10)
        var next = state.copy(
            plan = state.plan.copy(entries = entries),
            needs = needs,
            saleBuys = state.saleBuys + if (item.isSale) 1 else 0,
        )
        if (item.isSale) {
            next = addXp(next, 4, "Покупка со скидкой")
        }
        next = refreshMood(next)
        val msg = "Куплено «${item.name}» за ${item.cost}. ${item.effect} Баланс статьи «${item.category.label}»: ${next.plan.entry(item.category).left}."
        return EngineResult(next.withBadges(), msg)
    }

    fun care(state: GameSnapshot, category: SpendCategory): EngineResult {
        val action = careActions.first { it.category == category }
        if (!state.planConfirmed) {
            return EngineResult(state, "Сначала собери план недели — тогда будет понятно, из какой статьи напоить и покормить.", warning = true)
        }
        val left = state.plan.entry(category).left
        if (left < action.cost) {
            return EngineResult(
                state,
                "Не хватает ${action.cost - left} в статье «${category.label}». Выполни задание или подожди следующую неделю.",
                warning = true,
            )
        }
        val entries = state.plan.entries.map {
            if (it.category == category) it.copy(spent = it.spent + action.cost) else it
        }
        val needs = bumpNeed(state.needs, category, 22)
        var next = state.copy(
            plan = state.plan.copy(entries = entries),
            needs = needs,
            caredWater = state.caredWater || category == SpendCategory.WATER,
            caredFood = state.caredFood || category == SpendCategory.FOOD,
            caredPlay = state.caredPlay || category == SpendCategory.PLAY,
        )
        next = addXp(next, 1, action.label)
        next = refreshMood(next)
        return EngineResult(
            next.withBadges(),
            "${action.label}: −${action.cost} из «${category.label}». ${next.pet.moodNote}",
        )
    }

    fun deposit(state: GameSnapshot, amount: Int): EngineResult {
        if (amount <= 0) return EngineResult(state, "Нечего откладывать.", warning = true)
        val save = state.plan.entry(SpendCategory.SAVE)
        val take = amount.coerceAtMost(save.left.coerceAtLeast(0))
        if (take <= 0) {
            return EngineResult(state, "В статье «Копилка» пусто. Сначала заложи туда монеты в плане.", warning = true)
        }
        val entries = state.plan.entries.map {
            if (it.category == SpendCategory.SAVE) it.copy(spent = it.spent + take) else it
        }
        val goals = state.goals.map {
            if (it.id == state.selectedGoalId) it.copy(saved = it.saved + take) else it
        }
        val goal = goals.first { it.id == state.selectedGoalId }
        var next = state.copy(plan = state.plan.copy(entries = entries), goals = goals)
        next = addXp(next, 2, "Накопление")
        next = refreshMood(next)
        val weeks = weeksToGoal(goal, next.plan.entry(SpendCategory.SAVE).planned)
        val extra = if (weeks != null) " При таком темпе цель через $weeks нед." else ""
        return EngineResult(next.withBadges(), "Отложено $take. В копилке ${goal.saved} из ${goal.target}.$extra")
    }

    fun withdraw(state: GameSnapshot, amount: Int): EngineResult {
        val goal = state.selectedGoal
        val take = amount.coerceAtMost(goal.saved).coerceAtLeast(0)
        if (take <= 0) return EngineResult(state, "В копилке пока пусто.", warning = true)
        val goals = state.goals.map {
            if (it.id == state.selectedGoalId) it.copy(saved = it.saved - take) else it
        }
        val entries = state.plan.entries.map {
            if (it.category == SpendCategory.PLAY) it.copy(planned = it.planned + take) else it
        }
        val allocated = entries.sumOf { it.planned }
        val free = (state.plan.weeklyIncome - allocated).coerceAtLeast(0)
        val next = refreshMood(
            state.copy(
                goals = goals,
                plan = state.plan.copy(entries = entries, freeCoins = if (state.planConfirmed) state.plan.freeCoins else free),
            ),
        )
        val updated = next.selectedGoal
        val weeks = weeksToGoal(updated, next.plan.entry(SpendCategory.SAVE).planned)
        val delay = if (weeks != null) " Срок цели станет около $weeks нед." else ""
        return EngineResult(
            next.withBadges(),
            "Из копилки снято $take. Они вернулись в «Игры». Накоплено ${updated.saved}.$delay",
            warning = true,
        )
    }

    fun selectGoal(state: GameSnapshot, goalId: String): EngineResult {
        val goal = state.goals.firstOrNull { it.id == goalId } ?: return EngineResult(state, "Нет такой цели.", warning = true)
        val weeks = weeksToGoal(goal, state.plan.entry(SpendCategory.SAVE).planned)
        val extra = if (weeks != null) " Если откладывать как сейчас — примерно $weeks нед." else " Сначала заложи копилку в плане."
        return EngineResult(
            state.copy(selectedGoalId = goalId),
            "Цель: ${goal.title}. Нужно ${goal.remaining} монет.$extra",
        )
    }

    fun completeTask(state: GameSnapshot, taskId: String, correct: Boolean, reward: Int): EngineResult {
        val task = state.tasks.firstOrNull { it.id == taskId } ?: return EngineResult(state, "Задание не найдено.", warning = true)
        if (task.done) return EngineResult(state, "Это задание уже выполнено.")
        val coins = if (correct) reward else (reward / 2).coerceAtLeast(2)
        val tasks = state.tasks.map { if (it.id == taskId) it.copy(done = true, subtitle = "выполнено") else it }
        val nextPlan = if (state.planConfirmed) {
            val entries = state.plan.entries.map {
                if (it.category == SpendCategory.PLAY) it.copy(planned = it.planned + coins) else it
            }
            state.plan.copy(entries = entries)
        } else {
            state.plan.copy(freeCoins = state.plan.freeCoins + coins)
        }
        var next = state.copy(
            tasks = tasks,
            plan = nextPlan,
            earnedTotal = state.earnedTotal + coins,
        )
        next = addXp(next, if (correct) 6 else 3, "Задание")
        next = refreshMood(next)
        val where = if (state.planConfirmed) "в статью «Игры»" else "к свободным монетам"
        val msg = if (correct) {
            "Верно. +$coins $where. ${next.pet.name} это заметил."
        } else {
            "Можно лучше, но ты уже думаешь. +$coins $where. Прочитай разбор и попробуй иначе на следующей неделе."
        }
        return EngineResult(next.withBadges(), msg, warning = !correct)
    }

    fun dailyOpen(state: GameSnapshot, epochDay: Long): EngineResult {
        if (!state.onboarded) return EngineResult(state, "")
        if (state.lastOpenEpochDay == epochDay) return EngineResult(state, "")
        val consecutive = if (state.lastOpenEpochDay == epochDay - 1) {
            state.streak.count { it.done }.coerceAtMost(6) + 1
        } else {
            1
        }
        val maskDone = consecutive.coerceIn(1, 7)
        var next = state.copy(
            streak = streakFromMask((1 shl maskDone) - 1),
            lastOpenEpochDay = epochDay,
            pet = state.pet.copy(dayOfWeek = maskDone.coerceIn(1, 7)),
        )
        val bonus = if (maskDone == 7) 5 else 0
        if (bonus > 0) {
            next = next.copy(
                plan = if (next.planConfirmed) {
                    next.plan.copy(
                        entries = next.plan.entries.map {
                            if (it.category == SpendCategory.PLAY) it.copy(planned = it.planned + bonus) else it
                        },
                    )
                } else {
                    next.plan.copy(freeCoins = next.plan.freeCoins + bonus)
                },
                earnedTotal = next.earnedTotal + bonus,
            )
            return EngineResult(next, "Семь дней подряд! +$bonus монет за привычку заходить.")
        }
        return EngineResult(next, "День $maskDone из 7. На седьмой день +5 монет.")
    }

    fun setSound(state: GameSnapshot, on: Boolean) = state.copy(soundOn = on)

    fun setDemo(state: GameSnapshot, on: Boolean) = state.copy(demoMode = on)

    fun setIncome(state: GameSnapshot, income: Int): EngineResult {
        if (state.planConfirmed) {
            return EngineResult(state, "Доход сменится со следующей недели.", warning = true)
        }
        val next = state.copy(plan = emptyPlan(income).let { empty ->
            empty.copy(weeklyIncome = income, freeCoins = income)
        })
        return EngineResult(next.copy(plan = next.plan.copy(weeklyIncome = income, freeCoins = income)), "Теперь $income монет в неделю. Разложи план заново.")
    }

    fun closeWeek(state: GameSnapshot): EngineResult {
        if (!state.planConfirmed) {
            return EngineResult(state, "Сначала подтверди план, потом можно закрыть неделю.", warning = true)
        }
        val rows = state.plan.entries.map { ReportRow(it.category, it.planned, it.spent) }
        val spent = state.plan.entries.filter { it.category != SpendCategory.SAVE }.sumOf { it.spent }
        val savedNow = state.plan.entry(SpendCategory.SAVE).spent
        val over = rows.filter { it.category != SpendCategory.SAVE && it.isOver }
        val needsOk = state.needs.all { it.percent >= 50 } || (state.caredFood && state.caredWater)
        var xpGain = 0
        val log = mutableListOf<WeekLogEntry>()
        if (savedNow > 0) {
            xpGain += 10
            log += WeekLogEntry("Отложено в копилку", "+$savedNow", LogTone.GOOD, true)
        }
        if (over.isEmpty()) {
            xpGain += 10
            log += WeekLogEntry("План без перерасхода", "+10 опыта", LogTone.GOOD, false)
        } else {
            log += WeekLogEntry("Перерасход: ${over.joinToString { it.category.label }}", "−2", LogTone.BAD, true)
        }
        if (needsOk) {
            xpGain += 8
            log += WeekLogEntry("Нужное закрыто", "+8 опыта", LogTone.GOOD, false)
        } else {
            log += WeekLogEntry("Нужное закрыто не полностью", "Питомец немного устал", LogTone.BAD, false)
        }
        val savePercent = state.plan.percentOf(SpendCategory.SAVE)
        if (savePercent >= 20) xpGain += 6
        val note = when {
            over.isNotEmpty() -> "План чуть поехал. На следующей неделе урежь желаемое и верни копилку. Прогресс не сгорел."
            savedNow == 0 -> "Цель не двигалась. Попробуй сразу заложить 20% в копилку."
            else -> "Неделя сложилась: нужное закрыто, копилка выросла. Питомец подрос."
        }
        val summary = "План ${state.plan.weeklyIncome} · потрачено $spent · отложено $savedNow"
        val report = WeekReport(week = state.pet.dayOfWeek.coerceAtLeast(state.weeksDone + 1).let { state.weeksDone + 1 }, rows = rows, summary = summary, note = note)
        val savedValue = savedNow
        val tone = when {
            over.isNotEmpty() -> HistoryTone.BAD
            savedValue >= 12 -> HistoryTone.HIGH
            savedValue >= 8 -> HistoryTone.MID
            else -> HistoryTone.LOW
        }
        val history = (state.history + HistoryWeek("н${state.weeksDone + 1}", savedValue, tone, barHeight = (savedValue * 4).coerceIn(8, 70))).takeLast(5)
        val decayed = state.needs.map { need ->
            val cared = when (need.category) {
                SpendCategory.WATER -> state.caredWater
                SpendCategory.FOOD -> state.caredFood
                SpendCategory.PLAY -> state.caredPlay
                else -> true
            }
            need.copy(percent = (need.percent - if (cared) 12 else 28).coerceIn(8, 100))
        }
        var next = addXp(state, xpGain, "Итог недели")
        next = next.copy(
            plan = emptyPlan(next.plan.weeklyIncome),
            planConfirmed = false,
            needs = decayed,
            history = history,
            weekLog = log,
            report = report,
            weeksDone = state.weeksDone + 1,
            weeksOnTrack = if (over.isEmpty()) state.weeksOnTrack + 1 else 0,
            caredWater = false,
            caredFood = false,
            caredPlay = false,
            pet = next.pet.copy(dayOfWeek = 1, moodNote = note),
            earnedTotal = next.earnedTotal + next.plan.weeklyIncome,
        )
        next = next.copy(plan = emptyPlan(state.plan.weeklyIncome), earnedTotal = state.earnedTotal + xpGain + state.plan.weeklyIncome)
        // restore weekly income as free coins after close
        next = next.copy(
            plan = emptyPlan(state.plan.weeklyIncome),
            planConfirmed = false,
        )
        next = refreshMood(next).withBadges()
        return EngineResult(next, "Неделя ${report.week} закрыта. $note", reportReady = true)
    }

    fun repeatLastPlan(state: GameSnapshot, previous: WeekReport?): EngineResult {
        val rows = previous?.rows ?: return resetPlan(state)
        if (state.planConfirmed) return EngineResult(state, "Сначала закрой текущую неделю.", warning = true)
        val income = state.plan.weeklyIncome
        var entries = SpendCategory.entries.map { cat ->
            val planned = rows.firstOrNull { it.category == cat }?.planned ?: 0
            PlanEntry(cat, planned, 0)
        }
        var allocated = entries.sumOf { it.planned }
        if (allocated > income) {
            entries = entries.map { it.copy(planned = 0) }
            allocated = 0
        }
        val next = state.copy(plan = WeekPlan(entries, income - allocated, income), planConfirmed = false)
        return EngineResult(next, "Повторили прошлый план. Проверь и подтверди.")
    }

    fun parentBonus(state: GameSnapshot, amount: Int = 10): EngineResult {
        val next = if (state.planConfirmed) {
            state.copy(
                plan = state.plan.copy(
                    entries = state.plan.entries.map {
                        if (it.category == SpendCategory.PLAY) it.copy(planned = it.planned + amount) else it
                    },
                ),
                earnedTotal = state.earnedTotal + amount,
            )
        } else {
            state.copy(
                plan = state.plan.copy(freeCoins = state.plan.freeCoins + amount),
                earnedTotal = state.earnedTotal + amount,
            )
        }
        return EngineResult(next, "Взрослый добавил $amount монет. Это поддержка, а не оценка.")
    }

    fun decorate(state: GameSnapshot): GameSnapshot = refreshMood(state).withBadges()

    fun weeksToGoal(goal: SavingsGoal, weeklySave: Int): Int? {
        if (goal.saved >= goal.target) return 0
        if (weeklySave <= 0) return null
        return ceil((goal.target - goal.saved) / weeklySave.toFloat()).toInt()
    }

    private fun emptyPlan(income: Int) = WeekPlan(
        entries = SpendCategory.entries.map { PlanEntry(it, 0, 0) },
        freeCoins = income,
        weeklyIncome = income,
    )

    private fun starterNeeds() = listOf(
        NeedLevel(SpendCategory.WATER, 72),
        NeedLevel(SpendCategory.FOOD, 70),
        NeedLevel(SpendCategory.PLAY, 66),
    )

    private fun bumpNeed(needs: List<NeedLevel>, category: SpendCategory, by: Int) =
        needs.map { if (it.category == category) it.copy(percent = (it.percent + by).coerceAtMost(100)) else it }

    private fun streakFromMask(mask: Int): List<StreakDay> {
        val labels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        return labels.mapIndexed { index, label -> StreakDay(label, mask and (1 shl index) != 0) }
    }

    private fun addXp(state: GameSnapshot, amount: Int, reason: String): GameSnapshot {
        if (amount <= 0) return state
        val total = state.pet.totalXp + amount
        val index = stageIndexFor(total)
        val grew = index > state.pet.stageIndex
        val pet = state.pet.copy(
            totalXp = total,
            stageIndex = index,
            xp = xpPercentInStage(total),
            moodNote = if (grew) {
                "Новая стадия: ${growthStages[index].name}. $reason помог вырасти."
            } else {
                state.pet.moodNote
            },
        )
        return state.copy(pet = pet)
    }

    private fun refreshPet(pet: PetProfile, needs: List<NeedLevel>): PetProfile {
        val avg = needs.map { it.percent }.average()
        val mood = when {
            avg >= 70 -> PetMood.HAPPY
            avg >= 55 -> PetMood.OKAY
            avg >= 35 -> PetMood.BORED
            else -> PetMood.SAD
        }
        val note = when (mood) {
            PetMood.HAPPY -> "${pet.name} доволен: нужное закрыто."
            PetMood.OKAY -> "${pet.name} в порядке. Проверь, не забыта ли копилка."
            PetMood.BORED -> "${pet.name} скучает. Игры — желаемое, но без них грустно."
            PetMood.SAD -> "${pet.name} устал. Сначала еда и вода, желаемое можно подождать."
        }
        return pet.copy(
            mood = mood,
            moodNote = note,
            stageIndex = stageIndexFor(pet.totalXp),
            xp = xpPercentInStage(pet.totalXp),
        )
    }

    private fun refreshMood(state: GameSnapshot): GameSnapshot {
        val overspent = state.plan.entries.any { it.category != SpendCategory.SAVE && it.spent > it.planned && it.planned > 0 }
        var pet = refreshPet(state.pet, state.needs)
        if (overspent) {
            pet = pet.copy(
                moodNote = "План поехал: из какой-то статьи взяли лишнее. На следующей неделе вернём копилку. Прогресс на месте.",
            )
        }
        return state.copy(pet = pet)
    }

    private fun computeBadges(
        weeksOnTrack: Int,
        savePercent: Int,
        saleBuys: Int,
        goalPercent: Int,
        lastWeekClean: Int,
        stageIndex: Int,
    ): List<Badge> = listOf(
        Badge("План выполнен", "Уложиться в план 4 недели подряд", (weeksOnTrack * 100 / 4).coerceIn(0, 100)),
        Badge("Двадцать процентов", "Держать копилку ≥20%", if (savePercent >= 20) 100 else (savePercent * 5).coerceIn(0, 99)),
        Badge("Охотник за скидками", "10 покупок со скидкой", (saleBuys * 10).coerceIn(0, 100)),
        Badge("Полпути", "Половина цели собрана", goalPercent.coerceIn(0, 100)),
        Badge("Без долгов", "Неделя без перерасхода", lastWeekClean),
        Badge("Мудрый друг", "Вырастить питомца до мудрого друга", (stageIndex * 100 / 4).coerceIn(0, 100)),
    )

    private fun GameSnapshot.withBadges(): GameSnapshot {
        val savePercent = plan.percentOf(SpendCategory.SAVE)
        val lastClean = if (plan.entries.none { it.category != SpendCategory.SAVE && it.spent > it.planned && it.planned > 0 }) 100 else 0
        return copy(
            badges = computeBadges(
                weeksOnTrack = weeksOnTrack,
                savePercent = savePercent,
                saleBuys = saleBuys,
                goalPercent = selectedGoal.percent,
                lastWeekClean = lastClean,
                stageIndex = pet.stageIndex,
            ),
        )
    }
}
