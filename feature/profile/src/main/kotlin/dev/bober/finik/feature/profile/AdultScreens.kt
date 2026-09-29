package dev.bober.finik.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import dev.bober.finik.core.designsystem.component.FinikConfirmSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.component.BackHeader
import dev.bober.finik.core.designsystem.component.FinikCard
import dev.bober.finik.core.designsystem.component.FinikIcons
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.designsystem.theme.unbounded
import dev.bober.finik.core.model.GlossaryTerm
import dev.bober.finik.core.model.TodayEvent
import dev.bober.finik.core.model.WordOfDay
import dev.bober.finik.core.model.AiQuiz

@Composable
internal fun HelpScreen(
    terms: List<GlossaryTerm>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackHeader(title = "Подсказка", onBack = onBack)
        FinikCard(background = FinikColor.GreenCard, borderColor = null, gap = 10.dp) {
            Text(text = "Три решения каждую неделю", style = nunito(15), color = FinikColor.Ink)
            Text(
                text = "1. Потратить на нужное — еда и вода.\n2. Потратить на желаемое — игрушки, можно подождать.\n3. Отложить в копилку — на мечту.",
                style = nunito(14, FontWeight.SemiBold, lineHeight = 1.45),
                color = FinikColor.GreenInk40s,
            )
        }
        terms.forEach { term ->
            FinikCard(gap = 6.dp) {
                Text(text = term.term, style = nunito(15), color = FinikColor.Ink)
                Text(text = term.meaning, style = nunito(13, FontWeight.SemiBold, lineHeight = 1.4), color = FinikColor.Text46)
                if (term.example.isNotBlank()) {
                    Text(text = term.example, style = nunito(13, FontWeight.SemiBold, lineHeight = 1.4), color = FinikColor.Text42)
                }
            }
        }
    }
}

@Composable
internal fun AdultScreen(
    weeksDone: Int,
    saved: Int,
    stageName: String,
    tasksDone: Int,
    tasksTotal: Int,
    onlineExtras: Boolean,
    actionsEnabled: Boolean,
    wordOfDay: WordOfDay?,
    todayEvent: TodayEvent?,
    onBack: () -> Unit,
    onBonus: (String, Int, String) -> Unit,
    onLoadExtra: (String, (String) -> Unit) -> Unit,
    onLoadQuiz: ((AiQuiz?) -> Unit) -> Unit,
    onAnswerQuiz: (Int, Int, (String) -> Unit) -> Unit,
    onChooseEvent: (String, String) -> Unit,
    onChat: (String, (String) -> Unit) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var bonusPin by rememberSaveable { mutableStateOf("") }
    var bonusAmount by rememberSaveable { mutableStateOf("10") }
    var bonusReason by rememberSaveable { mutableStateOf("") }
    var showReset by rememberSaveable { mutableStateOf(false) }
    var extraText by rememberSaveable { mutableStateOf("") }
    var chatText by rememberSaveable { mutableStateOf("") }
    var chatReply by rememberSaveable { mutableStateOf("") }
    var quizFeedback by rememberSaveable { mutableStateOf("") }
    var quiz by androidx.compose.runtime.remember { mutableStateOf<AiQuiz?>(null) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackHeader(title = "Для взрослых", onBack = onBack)
        Text(
            text = "Зачем это приложение",
            style = unbounded(19, lineHeight = 1.2),
            color = FinikColor.Ink,
        )
        FinikCard(gap = 8.dp) {
            Text(
                text = "Ребёнок учится делить карманные монеты на нужное, желаемое и накопления. Оценки и сравнения с другими детьми здесь нет.",
                style = nunito(14, FontWeight.SemiBold, lineHeight = 1.45),
                color = FinikColor.Text42,
            )
        }
        FinikCard(gap = 8.dp) {
            Text(text = "Общий прогресс", style = nunito(15), color = FinikColor.Ink)
            Text(text = "Недель с планом: $weeksDone", style = nunito(14), color = FinikColor.Text46)
            Text(text = "Отложено на цель: $saved", style = nunito(14), color = FinikColor.Text46)
            Text(text = "Стадия питомца: $stageName", style = nunito(14), color = FinikColor.Text46)
            Text(text = "Темы: план, копилка, покупки · задания $tasksDone из $tasksTotal", style = nunito(14), color = FinikColor.Text46)
        }
        FinikCard(gap = 8.dp) {
            Text(text = "Поддержка взрослого", style = nunito(15), color = FinikColor.Ink)
            Text(
                text = "Начислите от 1 до 20 игровых монет за полезное дело. Укажите причину и подтвердите родительским PIN-кодом.",
                style = nunito(13, FontWeight.SemiBold, lineHeight = 1.4),
                color = FinikColor.Text46,
            )
        }
        TextField(value = bonusAmount, onValueChange = { bonusAmount = it.filter(Char::isDigit).take(2) }, label = { Text("Монет: 1–20") }, enabled = actionsEnabled, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        TextField(value = bonusReason, onValueChange = { bonusReason = it.take(120) }, label = { Text("За что начисляем?") }, enabled = actionsEnabled, modifier = Modifier.fillMaxWidth())
        TextField(value = bonusPin, onValueChange = { bonusPin = it.take(32) }, label = { Text("Родительский PIN") }, enabled = actionsEnabled, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), modifier = Modifier.fillMaxWidth())
        PrimaryButton(
            text = "Начислить бонус",
            onClick = { onBonus(bonusPin, bonusAmount.toIntOrNull() ?: 0, bonusReason); bonusPin = "" },
            enabled = actionsEnabled && bonusAmount.toIntOrNull() in 1..20 && bonusReason.isNotBlank() && bonusPin.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
        if (onlineExtras) {
            wordOfDay?.let { word ->
                FinikCard(gap = 5.dp) {
                    Text(text = "Слово дня: ${word.word}", style = nunito(15), color = FinikColor.Ink)
                    Text(text = word.meaning, style = nunito(13), color = FinikColor.Text46)
                }
            }
            todayEvent?.let { event ->
                FinikCard(gap = 7.dp) {
                    Text(text = event.title, style = nunito(15), color = FinikColor.Ink)
                    Text(text = event.text, style = nunito(13), color = FinikColor.Text46)
                    if (event.chosen == null) event.options.forEach { option ->
                        PrimaryButton(text = option.label, enabled = actionsEnabled, onClick = { onChooseEvent(event.id, option.key) }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            FinikCard(gap = 8.dp) {
                Text(text = "Дополнительные истории", style = nunito(15), color = FinikColor.Ink)
                listOf("diary" to "Дневник", "dream" to "Совет по мечте", "origin" to "История питомца", "summary" to "Итоги недели").forEach { (kind, title) ->
                    PrimaryButton(text = title, enabled = actionsEnabled, onClick = { onLoadExtra(kind) { extraText = it } }, modifier = Modifier.fillMaxWidth())
                }
                if (extraText.isNotBlank()) Text(text = extraText, style = nunito(13, lineHeight = 1.4), color = FinikColor.Text46)
            }
            FinikCard(gap = 8.dp) {
                Text(text = "Дополнительная викторина", style = nunito(15), color = FinikColor.Ink)
                PrimaryButton(text = "Получить вопросы", enabled = actionsEnabled, onClick = { onLoadQuiz { quiz = it } }, modifier = Modifier.fillMaxWidth())
                quiz?.questions?.forEach { question ->
                    Text(text = question.question, style = nunito(13), color = FinikColor.Ink)
                    question.options.forEachIndexed { answerIndex, option ->
                        PrimaryButton(
                            text = option,
                            enabled = actionsEnabled,
                            onClick = { onAnswerQuiz(question.index, answerIndex) { quizFeedback = it } },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                if (quizFeedback.isNotBlank()) Text(text = quizFeedback, style = nunito(13), color = FinikColor.Text46)
                Text(text = "За первый правильный ответ дня можно получить награду. Результат сохранится в профиле.", style = nunito(12), color = FinikColor.Text50)
            }
            FinikCard(gap = 8.dp) {
                Text(text = "Чат с совой", style = nunito(15), color = FinikColor.Ink)
                TextField(
                    value = chatText,
                    onValueChange = { chatText = it.take(500) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Сообщение") },
                    maxLines = 4,
                )
                PrimaryButton(
                    text = "Спросить",
                    enabled = actionsEnabled && chatText.isNotBlank(),
                    onClick = { if (chatText.isNotBlank()) onChat(chatText) { chatReply = it } },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (chatReply.isNotBlank()) Text(text = chatReply, style = nunito(13, lineHeight = 1.4), color = FinikColor.Text46)
            }
        }
        SettingsRow(
            icon = { SettingsIcon(FinikIcons.Reset, FinikColor.RedIcon) },
            title = "Удалить профиль и данные",
            titleColor = FinikColor.RedReset,
            borderColor = FinikColor.RedBorderReset,
            onClick = { if (actionsEnabled) showReset = true },
        )
    }
    if (showReset) FinikConfirmSheet(
        title = "Сбросить профиль?",
        body = "Монеты, питомец и достижения текущего профиля будут удалены с сервера. Это действие нельзя отменить.",
        confirmText = "Сбросить",
        warning = true,
        onConfirm = { showReset = false; onReset() },
        onDismiss = { showReset = false },
    )
}
