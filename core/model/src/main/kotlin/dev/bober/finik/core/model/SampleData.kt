package dev.bober.finik.core.model

/**
 * Данные-заглушка, повторяющие начальное состояние прототипа «Финик 8–11».
 * Нужны, чтобы экраны выглядели как в макете до появления логики и хранилища.
 */
object SampleData {

    const val WEEKLY_INCOME = 40
    const val GOAL_TARGET = 150
    const val FREE_COINS = 12

    val pet = PetProfile(
        name = "Финик",
        species = PetSpecies.FINIK,
        stageIndex = 2,
        xp = 55,
        mood = PetMood.OKAY,
        moodNote = "Финик скучает. Проверь, в какой статье остались монеты.",
        dayOfWeek = 3,
    )

    val needs: List<NeedLevel> = listOf(
        NeedLevel(SpendCategory.WATER, 58),
        NeedLevel(SpendCategory.FOOD, 64),
        NeedLevel(SpendCategory.PLAY, 46),
    )

    val plan = WeekPlan(
        entries = listOf(
            PlanEntry(SpendCategory.FOOD, planned = 14, spent = 6),
            PlanEntry(SpendCategory.WATER, planned = 4, spent = 2),
            PlanEntry(SpendCategory.PLAY, planned = 6, spent = 2),
            PlanEntry(SpendCategory.SAVE, planned = 4, spent = 0),
        ),
        freeCoins = FREE_COINS,
        weeklyIncome = WEEKLY_INCOME,
    )

    val careActions: List<CareAction> = listOf(
        CareAction(label = "Полить", category = SpendCategory.WATER, cost = 2),
        CareAction(label = "Покормить", category = SpendCategory.FOOD, cost = 3),
        CareAction(label = "Поиграть", category = SpendCategory.PLAY, cost = 2),
    )

    const val STREAK = 3
    val streakDays: List<StreakDay> = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        .mapIndexed { index, label -> StreakDay(label = label, done = index < STREAK) }

    val incomeOptions: List<Pair<Int, String>> = listOf(20 to "сложно", 40 to "обычно", 60 to "легко")

    val tasks: List<TaskItem> = listOf(
        TaskItem("q1", "Мини-урок: цена и скидка", "3 вопроса", TaskKind.LESSON, reward = 6, done = false, target = TaskTarget.QUIZ),
        TaskItem("t2", "Отложи 10 монет до конца недели", "осталось 4 дня", TaskKind.WEEK, reward = 8, done = false, target = TaskTarget.GOAL),
        TaskItem("t3", "Уложись в план по еде", "потрачено 6 из 14", TaskKind.WEEK, reward = 10, done = false, target = TaskTarget.PLAN),
        TaskItem("t4", "Заходи 7 дней подряд", "3 из 7", TaskKind.HABIT, reward = 5, done = false),
        TaskItem("t5", "Купи что-то со скидкой", "в лавке есть −40%", TaskKind.DAY, reward = 4, done = true, target = TaskTarget.SHOP),
    )

    val quiz: List<QuizQuestion> = listOf(
        QuizQuestion(
            question = "Корм стоит 10, скидка 40%. Сколько заплатишь?",
            options = listOf("4 монеты", "6 монет", "8 монет"),
            rightIndex = 1,
            explanation = "40% от 10 — это 4. Значит платишь 10 − 4 = 6.",
        ),
        QuizQuestion(
            question = "Доход 40 монет. Сколько это 20% в копилку?",
            options = listOf("4 монеты", "8 монет", "20 монет"),
            rightIndex = 1,
            explanation = "20% — это пятая часть: 40 ÷ 5 = 8.",
        ),
        QuizQuestion(
            question = "В плане на еду 14, потратил 18. Что произошло?",
            options = listOf("Перерасход на 4", "Сэкономил 4", "Ничего"),
            rightIndex = 0,
            explanation = "Лишние 4 монеты придётся взять из другой статьи — чаще всего из копилки.",
        ),
    )

    val shop: List<ShopItem> = listOf(
        ShopItem("s1", "Вода, 3 дня", cost = 3, category = SpendCategory.WATER, glyph = ItemGlyph.CIRCLE),
        ShopItem("s2", "Витамины", cost = 6, oldCost = 10, category = SpendCategory.FOOD, glyph = ItemGlyph.ROUNDED),
        ShopItem("s3", "Корм на неделю", cost = 12, category = SpendCategory.FOOD, glyph = ItemGlyph.POT),
        ShopItem("s4", "Мячик", cost = 3, oldCost = 5, category = SpendCategory.PLAY, glyph = ItemGlyph.CIRCLE),
        ShopItem("s5", "Новый горшок", cost = 18, category = SpendCategory.PLAY, glyph = ItemGlyph.TALL_POT),
        ShopItem("s6", "Лейка получше", cost = 8, oldCost = 11, category = SpendCategory.WATER, glyph = ItemGlyph.BUCKET),
    )

    val goal = SavingsGoal(title = "Солнечное окно и большой горшок", target = GOAL_TARGET, saved = 62)

    val history: List<HistoryWeek> = listOf(
        HistoryWeek("н1", 6, HistoryTone.LOW, barHeight = 30),
        HistoryWeek("н2", 14, HistoryTone.HIGH, barHeight = 62),
        HistoryWeek("н3", 2, HistoryTone.BAD, barHeight = 12),
        HistoryWeek("н4", 11, HistoryTone.MID, barHeight = 50),
    )
    const val HISTORY_NOTE = "На третьей неделе почти всё ушло на игры — цель сдвинулась на две недели."

    val weekLog: List<WeekLogEntry> = listOf(
        WeekLogEntry("Отложено в копилку", "+11", LogTone.GOOD, rounded = true),
        WeekLogEntry("Куплено со скидкой", "+4 опыта", LogTone.NEUTRAL, rounded = false),
        WeekLogEntry("Перерасход по еде", "−2", LogTone.BAD, rounded = true),
        WeekLogEntry("Забыл полить во вторник", "−5 опыта", LogTone.BAD, rounded = false),
    )

    val badges: List<Badge> = listOf(
        Badge("План выполнен", "Уложиться в план 4 недели подряд", 75),
        Badge("Двадцать процентов", "Держать копилку ≥20% месяц", 100),
        Badge("Охотник за скидками", "10 покупок со скидкой", 60),
        Badge("Полпути", "Половина цели собрана", 41),
        Badge("Без долгов", "Неделя без перерасхода", 100),
        Badge("Большое дерево", "Вырастить Финика до дерева", 50),
    )

    const val EARNED_TOTAL = 214
    const val SAVED_TOTAL = 62
    const val WEEKS_DONE = 4
    const val SOUND_ON = true

    /** Отчёт «Неделя 4 закрыта» (вариант 2c). */
    const val REPORT_WEEK = 4
    val reportRows: List<ReportRow> = listOf(
        ReportRow(SpendCategory.FOOD, planned = 16, actual = 18),
        ReportRow(SpendCategory.PLAY, planned = 9, actual = 5),
        ReportRow(SpendCategory.SAVE, planned = 11, actual = 7),
    )
    const val REPORT_SUMMARY = "План 40 · потрачено 33 · отложено 7"
    const val REPORT_NOTE = "Еда вышла за план на 2 монеты — пришлось взять из копилки. Финик вырос медленнее."
}
