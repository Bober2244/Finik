package dev.bober.finik.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
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
            .background(FinikColor.Background)
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
    wordOfDay: WordOfDay?,
    todayEvent: TodayEvent?,
    onBack: () -> Unit,
    onToggleOnlineExtras: (Boolean) -> Unit,
    onBonus: () -> Unit,
    onLoadExtra: (String, (String) -> Unit) -> Unit,
    onLoadQuiz: ((AiQuiz?) -> Unit) -> Unit,
    onAnswerQuiz: (Int, Int, (String) -> Unit) -> Unit,
    onChooseEvent: (String, String) -> Unit,
    onChat: (String, (String) -> Unit) -> Unit,
    onReset: () -> Unit,
    onLocalReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showReset by rememberSaveable { mutableStateOf(false) }
    var showLocalReset by rememberSaveable { mutableStateOf(false) }
    var extraText by rememberSaveable { mutableStateOf("") }
    var chatText by rememberSaveable { mutableStateOf("") }
    var chatReply by rememberSaveable { mutableStateOf("") }
    var quizFeedback by rememberSaveable { mutableStateOf("") }
    var quiz by androidx.compose.runtime.remember { mutableStateOf<AiQuiz?>(null) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FinikColor.Background)
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
                text = "Добавьте 10 игровых монет. Операция сохранится в истории доходов.",
                style = nunito(13, FontWeight.SemiBold, lineHeight = 1.4),
                color = FinikColor.Text46,
            )
        }
        PrimaryButton(
            text = "Добавить 10 монет поддержки",
            onClick = onBonus,
            modifier = Modifier.fillMaxWidth(),
        )
        FinikCard(gap = 8.dp) {
            Text(text = "Сетевые материалы", style = nunito(15), color = FinikColor.Ink)
            Text(
                text = "При включении приложение отправит случайный идентификатор серверу. Игровые монеты, прогресс и копилка останутся только на этом устройстве. Для подключения нужен HTTPS сервер.",
                style = nunito(13, FontWeight.SemiBold, lineHeight = 1.4), color = FinikColor.Text46,
            )
            SettingsRow(
                icon = { SettingsIcon(FinikIcons.Adult, FinikColor.Green) },
                title = "Включить сетевые материалы",
                onClick = { onToggleOnlineExtras(!onlineExtras) },
            ) { ToggleLite(onlineExtras) }
        }
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
                        PrimaryButton(text = option.label, onClick = { onChooseEvent(event.id, option.key) }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            FinikCard(gap = 8.dp) {
                Text(text = "Дополнительные истории", style = nunito(15), color = FinikColor.Ink)
                listOf("diary" to "Дневник", "dream" to "Совет по мечте", "origin" to "История питомца", "summary" to "Итоги недели").forEach { (kind, title) ->
                    PrimaryButton(text = title, onClick = { onLoadExtra(kind) { extraText = it } }, modifier = Modifier.fillMaxWidth())
                }
                if (extraText.isNotBlank()) Text(text = extraText, style = nunito(13, lineHeight = 1.4), color = FinikColor.Text46)
            }
            FinikCard(gap = 8.dp) {
                Text(text = "Дополнительная викторина", style = nunito(15), color = FinikColor.Ink)
                PrimaryButton(text = "Получить вопросы", onClick = { onLoadQuiz { quiz = it } }, modifier = Modifier.fillMaxWidth())
                quiz?.questions?.forEach { question ->
                    Text(text = question.question, style = nunito(13), color = FinikColor.Ink)
                    question.options.forEachIndexed { answerIndex, option ->
                        PrimaryButton(
                            text = option,
                            onClick = { onAnswerQuiz(question.index, answerIndex) { quizFeedback = it } },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                if (quizFeedback.isNotBlank()) Text(text = quizFeedback, style = nunito(13), color = FinikColor.Text46)
                Text(text = "Ответы не меняют локальные игровые монеты.", style = nunito(12), color = FinikColor.Text50)
            }
            FinikCard(gap = 8.dp) {
                Text(text = "Свободный AI-чат для взрослого", style = nunito(15), color = FinikColor.Ink)
                TextField(
                    value = chatText,
                    onValueChange = { chatText = it.take(500) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Сообщение") },
                    maxLines = 4,
                )
                PrimaryButton(
                    text = "Спросить",
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
            onClick = { showReset = true },
        )
        SettingsRow(
            icon = { SettingsIcon(FinikIcons.Reset, FinikColor.RedIcon) },
            title = "Удалить только на устройстве",
            titleColor = FinikColor.RedReset,
            borderColor = FinikColor.RedBorderReset,
            onClick = { showLocalReset = true },
        )
    }
    if (showReset) FinikConfirmSheet(
        title = "Сбросить профиль?",
        body = "Монеты, питомец и достижения на этом устройстве будут удалены.",
        confirmText = "Сбросить",
        warning = true,
        onConfirm = { showReset = false; onReset() },
        onDismiss = { showReset = false },
    )
    if (showLocalReset) FinikConfirmSheet(
        title = "Удалить только локальный профиль?",
        body = "Игровой прогресс на этом устройстве исчезнет. Если ранее использовался сервер, его данные останутся там.",
        confirmText = "Удалить локально",
        warning = true,
        onConfirm = { showLocalReset = false; onLocalReset() },
        onDismiss = { showLocalReset = false },
    )
}

@Composable
private fun ToggleLite(on: Boolean) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .background(if (on) FinikColor.Green else FinikColor.BorderStrong, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .padding(3.dp)
            .then(Modifier),
    ) {
        Text(
            text = if (on) "вкл" else "выкл",
            style = nunito(12),
            color = if (on) MaterialTheme.colorScheme.onPrimary else FinikColor.Ink,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
