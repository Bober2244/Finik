package dev.bober.finik.core.data.content

import dev.bober.finik.core.model.GlossaryTerm
import dev.bober.finik.core.model.QuizQuestion
import dev.bober.finik.core.model.SampleData
import dev.bober.finik.core.model.SavingsGoal
import dev.bober.finik.core.model.ScenarioChoice
import dev.bober.finik.core.model.ScenarioTask
import dev.bober.finik.core.model.ShopItem
import dev.bober.finik.core.model.TaskItem

/**
 * Учебный контент отделён от UI. Новый сценарий добавляется сюда (или в JSON с сервера),
 * без переписывания экранов и правил списания монет.
 */
class ContentCatalog(
    private val extraTasks: List<TaskItem> = emptyList(),
    private val extraScenarios: List<ScenarioTask> = emptyList(),
) {
    val shop: List<ShopItem> = SampleData.shop

    val goals: List<SavingsGoal> = listOf(
        SavingsGoal("sunny_window", "Солнечное окно и большой горшок", 150, 0, why = "Ростку нужен свет и место для корней — это большая, но понятная мечта."),
        SavingsGoal("watering_kit", "Набор для полива", 80, 0, why = "Лейка и запас воды. Обязательный уход станет проще."),
        SavingsGoal("play_garden", "Игровая клумба", 120, 0, why = "Место для игр. Это желаемое: можно подождать, если копилка тонкая."),
    )

    val tasks: List<TaskItem> get() = bundledTasks + extraTasks

    val scenarios: List<ScenarioTask> get() = bundledScenarios + extraScenarios

    val quiz: List<QuizQuestion> = SampleData.quiz

    val glossary: List<GlossaryTerm> = listOf(
        GlossaryTerm("План", "Как ты заранее делишь монеты: на нужное, желаемое и копилку."),
        GlossaryTerm("Нужное", "То, без чего питомцу плохо: еда и вода. Это обязательные расходы."),
        GlossaryTerm("Желаемое", "Игрушки и украшения. Можно подождать, если монет мало."),
        GlossaryTerm("Копилка", "Монеты, которые ты откладываешь на мечту и не тратишь сразу."),
        GlossaryTerm("Цель", "Мечта с понятной ценой. Видно, сколько уже собрано и сколько осталось."),
        GlossaryTerm("Факт", "Сколько на самом деле потратил за неделю — сравниваем с планом."),
        GlossaryTerm("Игровая валюта", "Монеты только внутри игры. Настоящие деньги сюда не входят."),
    )

    fun scenario(id: String): ScenarioTask? = scenarios.firstOrNull { it.id == id }

    companion object {
        val bundledTasks: List<TaskItem> = SampleData.tasks

        val bundledScenarios: List<ScenarioTask> = listOf(
            ScenarioTask(
                id = "s-plan-1",
                prompt = "У тебя 40 монет. Корм стоит 16, мячик 20, в копилку хочется 10. Что сделаешь?",
                choices = listOf(
                    ScenarioChoice("Купить всё: корм и мячик, копилку потом", false, "Так расходы больше дохода. Сначала нужное и копилка, мячик можно на следующей неделе."),
                    ScenarioChoice("Корм и 8 в копилку, мячик подождать", true, "Нужное закрыто, копилка растёт, желаемое подождёт. Это и есть план."),
                    ScenarioChoice("Только мячик, корм как-нибудь", false, "Игрушка — желаемое. Без корма питомец устанет, а цель не приблизится."),
                ),
            ),
            ScenarioTask(
                id = "s-plan-2",
                prompt = "Доход 40 монет. Сколько это 20% в копилку?",
                choices = listOf(
                    ScenarioChoice("4 монеты", false, "4 — это 10%. 20% — пятая часть: 40 ÷ 5 = 8."),
                    ScenarioChoice("8 монет", true, "Верно. 20% — это 8. Если каждый раз откладывать столько, цель ближе."),
                    ScenarioChoice("20 монет", false, "20 — это половина, 50%. Для копилки достаточно начать с 20%."),
                ),
            ),
            ScenarioTask(
                id = "s-save-1",
                prompt = "До цели 12 монет. Сегодня в лавке мячик со скидкой за 3. Что разумнее?",
                choices = listOf(
                    ScenarioChoice("Купить мячик: скидка же", false, "Скидка — хорошо, но цель уже рядом. Желаемое подождёт одну неделю."),
                    ScenarioChoice("Отложить 12 и закрыть цель", true, "Цель важнее желаемого, когда до неё рукой подать. Мячик не убежит."),
                    ScenarioChoice("Снять из копилки на мячик", false, "Снимать с мечты можно, но тогда цель отъедет. Сначала спроси себя: это нужно сейчас?"),
                ),
            ),
            ScenarioTask(
                id = "s-save-2",
                prompt = "Хочется снять 10 из копилки на качели. Что случится?",
                choices = listOf(
                    ScenarioChoice("Цель отодвинется, это нормально, если очень надо", true, "Снимать можно, но только после паузы. Приложение покажет, как уменьшится копилка и срок мечты."),
                    ScenarioChoice("Ничего: копилка бесконечная", false, "Копилка — это твои отложенные монеты. Если снять, мечта станет дальше."),
                    ScenarioChoice("Питомец исчезнет", false, "Ошибка не наказывает питомца навсегда. Можно поправить план на следующей неделе."),
                ),
            ),
            ScenarioTask(
                id = "s-buy-1",
                prompt = "Корм стоит 12, в статье «Еда» осталось 7. Как быть?",
                choices = listOf(
                    ScenarioChoice("Купить в долг, баланс уйдёт в минус", false, "Минуса нет: нельзя купить то, на что не хватает. Это защита, не наказание."),
                    ScenarioChoice("Выполнить задание, докупить монет или взять более дешёвое", true, "Не хватает 5. Варианты: задание, подождать неделю, выбрать дешевле. Желаемое можно вычеркнуть."),
                    ScenarioChoice("Купить игрушку вместо корма", false, "Игрушка — желаемое. Сначала нужное: еда и вода."),
                ),
            ),
        )

        fun withRemote(tasks: List<TaskItem>, scenarios: List<ScenarioTask>) =
            ContentCatalog(extraTasks = tasks, extraScenarios = scenarios)
    }
}
