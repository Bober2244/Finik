package dev.bober.finik.feature.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.bober.finik.core.data.FinikViewModel
import dev.bober.finik.core.designsystem.component.OutlineButton
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.model.Adventure
import dev.bober.finik.core.model.AdventureAction
import dev.bober.finik.core.model.LearningReview
import dev.bober.finik.core.model.LessonAnswer
import dev.bober.finik.core.model.QuizQuestion
import dev.bober.finik.core.model.ReviewAnswer
import dev.bober.finik.core.model.TaskItem
import dev.bober.finik.core.model.TaskTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TaskSessionSheet(
    task: TaskItem,
    vm: FinikViewModel,
    onClose: () -> Unit,
    onOpenPlan: () -> Unit,
    onOpenGoal: () -> Unit,
    onOpenShop: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val enabled = state.online && !busy
    ModalBottomSheet(onDismissRequest = onClose) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(task.title, style = nunito(21))
            Text(task.subtitle, style = nunito(14))
            if (!state.online) {
                Text("Для выполнения задания нужно подключение.", color = FinikColor.RedReset)
                OutlineButton(text = "Закрыть и восстановить связь", enabled = !busy, onClick = { onClose(); vm.refresh() }, modifier = Modifier.fillMaxWidth())
            }
            when {
                task.rewarded -> Text("Награда за эту неделю уже получена.")
                task.activity == "ADVENTURE" -> AdventureContent(task, vm, enabled, retryEnabled = !busy)
                task.done -> PrimaryButton(text = "Забрать ${task.reward} монет", onClick = { vm.claimTask(task.id) }, enabled = enabled, modifier = Modifier.fillMaxWidth())
                task.target == TaskTarget.QUIZ || task.target == TaskTarget.SCENARIO -> LessonContent(task, vm, enabled, retryEnabled = !busy)
                else -> {
                    Text("Прогресс: ${task.progress} / ${task.goalCount}. Выполни действия из описания и вернись за наградой.")
                    val destination = when (task.target) {
                        TaskTarget.PLAN -> "Открыть план" to onOpenPlan
                        TaskTarget.GOAL -> "Открыть копилку" to onOpenGoal
                        TaskTarget.SHOP -> "Открыть лавку" to onOpenShop
                        else -> null
                    }
                    destination?.let { (label, action) -> OutlineButton(text = label, onClick = action, modifier = Modifier.fillMaxWidth()) }
                }
            }
        }
    }
}

@Composable
private fun LessonContent(task: TaskItem, vm: FinikViewModel, enabled: Boolean, retryEnabled: Boolean) {
    var questions by remember(task.id) { mutableStateOf<List<QuizQuestion>?>(null) }
    var index by remember(task.id) { mutableIntStateOf(0) }
    var answer by remember(task.id) { mutableStateOf<LessonAnswer?>(null) }
    var number by remember(task.id) { mutableStateOf("") }
    var hint by remember(task.id) { mutableStateOf("") }
    var pending by remember(task.id) { mutableStateOf(false) }
    LaunchedEffect(task.id) { vm.loadTaskQuestions(task.id) { questions = it } }
    val question = questions?.getOrNull(index)
    when {
        questions == null -> CircularProgressIndicator()
        questions.isNullOrEmpty() -> {
            Text("Не удалось загрузить вопросы.")
            OutlineButton(text = "Повторить", enabled = retryEnabled && !pending, onClick = {
                pending = true
                vm.loadTaskQuestions(task.id) { questions = it; pending = false }
            })
        }
        answer?.lessonDone == true -> PrimaryButton(text = "Забрать ${task.reward} монет", onClick = { vm.claimTask(task.id) }, enabled = enabled, modifier = Modifier.fillMaxWidth())
        question != null -> {
            Text("Вопрос ${index + 1} из ${questions?.size}", style = nunito(13), color = FinikColor.Text50)
            if (question.scene.isNotBlank()) Text(question.scene, style = nunito(14))
            Text(question.question, style = nunito(17))
            val canAnswer = enabled && !pending && answer?.correct != true
            if (question.activity == "COINS") {
                NumberField(value = number, label = "Сколько монет?", enabled = canAnswer, onChange = { number = it })
                PrimaryButton(text = "Ответить", enabled = canAnswer && number.toIntOrNull() in 0..100, modifier = Modifier.fillMaxWidth(), onClick = {
                    pending = true
                    vm.answerTask(task.id, question.slug, answerValue = number.toIntOrNull()) { answer = it; pending = false }
                })
            } else question.options.forEachIndexed { answerIndex, option ->
                OutlineButton(text = option, enabled = canAnswer, modifier = Modifier.fillMaxWidth(), onClick = {
                    pending = true
                    vm.answerTask(task.id, question.slug, answerIndex = answerIndex) { answer = it; pending = false }
                })
            }
            answer?.let { result ->
                Text((if (result.correct) "Верно! " else "Попробуй ещё раз. ") + result.explanation, style = nunito(14))
                if (result.correct) PrimaryButton(text = "Следующий вопрос", onClick = {
                    index = (index + 1) % (questions?.size ?: 1)
                    answer = null; number = ""; hint = ""
                }, modifier = Modifier.fillMaxWidth())
            }
            OutlineButton(text = "Подсказка", enabled = enabled && !pending, onClick = {
                pending = true
                vm.taskHint(task.id, question.slug) { hint = it ?: "Подсказка сейчас недоступна."; pending = false }
            })
            if (hint.isNotBlank()) Text(hint, style = nunito(14))
        }
    }
}

@Composable
private fun AdventureContent(task: TaskItem, vm: FinikViewModel, enabled: Boolean, retryEnabled: Boolean) {
    var adventure by remember(task.id) { mutableStateOf<Adventure?>(null) }
    var loading by remember(task.id) { mutableStateOf(true) }
    var pending by remember(task.id) { mutableStateOf(false) }
    LaunchedEffect(task.id) { vm.loadAdventure(task.id) { adventure = it; loading = false } }
    if (loading) { CircularProgressIndicator(); return }
    val game = adventure
    if (game == null) {
        Text("Не удалось загрузить экспедицию.")
        OutlineButton(text = "Повторить", enabled = retryEnabled, onClick = { loading = true; vm.loadAdventure(task.id) { adventure = it; loading = false } })
        return
    }
    var food by remember(game.stage) { mutableStateOf("") }
    var water by remember(game.stage) { mutableStateOf("") }
    var reserve by remember(game.stage) { mutableStateOf("") }
    var amount by remember(game.stage) { mutableStateOf("") }
    val canAct = enabled && !pending
    val play: (AdventureAction) -> Unit = { action ->
        pending = true
        vm.playAdventure(task.id, action) { result -> if (result != null) adventure = result; pending = false }
    }
    Text("Этап ${game.stage.coerceAtMost(game.total - 1) + 1}/${game.total} · кошелёк ${game.wallet} · запас ${game.reserve}", style = nunito(13))
    Text("Монеты экспедиции используются только в этой истории.", style = nunito(12), color = FinikColor.Text50)
    if (game.feedback.isNotBlank()) Text(game.feedback, style = nunito(14))
    if (game.petReaction.isNotBlank()) Text(game.petReaction, style = nunito(14), color = FinikColor.Green)
    Text(game.scene.title, style = nunito(18))
    Text(game.scene.story, style = nunito(15, lineHeight = 1.4))
    when {
        game.completed -> {
            Text("Медаль: ${game.medal ?: "участник"} · на мечту: ${game.dream}")
            if (!game.rewarded) PrimaryButton(text = "Забрать ${task.reward} монет", enabled = canAct, onClick = { vm.claimTask(task.id) }, modifier = Modifier.fillMaxWidth())
            OutlineButton(text = "Пройти заново", enabled = canAct, onClick = {
                pending = true
                vm.restartAdventure(task.id) { result -> if (result != null) adventure = result; pending = false }
            })
        }
        game.scene.kind == "BUDGET" -> {
            Text("Нужно на еду: ${game.scene.foodNeed}, на воду: ${game.scene.waterNeed}. Совет по запасу: ${game.scene.reserveHint}.", style = nunito(14))
            NumberField(food, "Еда", canAct) { food = it }
            NumberField(water, "Вода", canAct) { water = it }
            NumberField(reserve, "Запас", canAct) { reserve = it }
            val values = listOf(food.toIntOrNull(), water.toIntOrNull(), reserve.toIntOrNull())
            val valid = values.all { it != null && it in 0..100 } && values.filterNotNull().sum() <= game.wallet
            PrimaryButton(text = "Отправиться", enabled = canAct && valid, modifier = Modifier.fillMaxWidth(), onClick = {
                play(AdventureAction(game.stage, food.toIntOrNull(), water.toIntOrNull(), reserve.toIntOrNull()))
            })
        }
        game.scene.kind == "SAVE" -> {
            NumberField(amount, "Отложить монет", canAct) { amount = it }
            PrimaryButton(text = "Завершить историю", enabled = canAct && amount.toIntOrNull() in 0..game.wallet, modifier = Modifier.fillMaxWidth(), onClick = {
                play(AdventureAction(game.stage, amount = amount.toIntOrNull()))
            })
        }
        else -> game.scene.choices.forEach { choice ->
            OutlineButton(text = "${choice.title} · ${choice.cost} мон.", enabled = canAct, modifier = Modifier.fillMaxWidth(), onClick = {
                play(AdventureAction(game.stage, choiceId = choice.id))
            })
            if (choice.detail.isNotBlank()) Text(choice.detail, style = nunito(13), color = FinikColor.Text50)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReviewSheet(vm: FinikViewModel, onClose: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    var review by remember { mutableStateOf<LearningReview?>(null) }
    var answer by remember { mutableStateOf<ReviewAnswer?>(null) }
    var loading by remember { mutableStateOf(true) }
    var pending by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { vm.loadReview { review = it; loading = false } }
    ModalBottomSheet(onDismissRequest = onClose) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Повторение тем", style = nunito(21))
            if (!state.online) OutlineButton(text = "Закрыть и восстановить связь", enabled = !busy, onClick = { onClose(); vm.refresh() }, modifier = Modifier.fillMaxWidth())
            val item = review
            if (loading) CircularProgressIndicator()
            else if (item == null) {
                Text("Не удалось загрузить повторение.")
                OutlineButton(text = "Повторить", enabled = !busy, onClick = { loading = true; vm.loadReview { review = it; loading = false } })
            } else {
                val topic = item.topic
                Text(item.title, style = nunito(17))
                Text(item.question, style = nunito(15))
                if (topic == null) Text(item.nextDue?.let { "Следующее повторение: ${it.take(10)}" } ?: "На сегодня всё. Новые темы появятся после уроков.")
                else item.options.forEachIndexed { index, option ->
                    OutlineButton(text = option, enabled = state.online && !busy && !pending && answer == null, modifier = Modifier.fillMaxWidth(), onClick = {
                        pending = true
                        vm.answerReview(topic, index) { answer = it; pending = false }
                    })
                }
                answer?.let { result ->
                    Text((if (result.correct) "Верно! " else "Разберём ответ. ") + result.explanation, style = nunito(14))
                    PrimaryButton(text = "Продолжить", enabled = state.online && !busy, onClick = {
                        loading = true; answer = null
                        vm.loadReview { review = it; loading = false }
                    }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun NumberField(value: String, label: String, enabled: Boolean, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = { onChange(it.filter(Char::isDigit).take(3)) }, label = { Text(label) }, enabled = enabled, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
}
