package dev.bober.finik.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.bober.finik.core.data.FinikViewModel
import dev.bober.finik.core.designsystem.component.OutlineButton
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.designsystem.theme.nunito

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeExtrasSheet(vm: FinikViewModel, onClose: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    var message by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf(false) }
    val enabled = state.online && !busy && !pending
    ModalBottomSheet(onDismissRequest = onClose) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("В гостях у ${state.pet.name}", style = nunito(21))
            if (!state.online) Text("Подключись к интернету, чтобы поговорить с совой.")
            listOf("diary" to "Дневник совы", "dream" to "Как накопить на мечту", "origin" to "История совы", "summary" to "Итоги недели").forEach { (kind, title) ->
                OutlineButton(text = title, enabled = enabled, modifier = Modifier.fillMaxWidth(), onClick = {
                    pending = true
                    vm.loadExtra(kind) { answer = it; pending = false }
                })
            }
            TextField(
                value = message,
                onValueChange = { message = it.take(500) },
                label = { Text("Спроси сову о деньгах и планах") },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
            )
            PrimaryButton(text = if (pending) "Сова думает…" else "Спросить", enabled = enabled && message.isNotBlank(), modifier = Modifier.fillMaxWidth(), onClick = {
                pending = true
                vm.chat(message) { answer = it; pending = false }
            })
            if (answer.isNotBlank()) Text(answer, style = nunito(15, lineHeight = 1.45))
        }
    }
}
