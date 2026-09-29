package dev.bober.finik.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.nunito

/** Debug connection editor. The caller checks and activates the address atomically. */
@Composable
fun ServerConnectionDialog(
    currentAddress: String,
    busy: Boolean,
    onConnect: (String, (Boolean, String) -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by rememberSaveable(currentAddress) { mutableStateOf(currentAddress) }
    var pending by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    AlertDialog(
        onDismissRequest = { if (!pending) onDismiss() },
        title = { Text("Адрес сервера", style = nunito(21)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Текущий: ${currentAddress.ifBlank { "не задан" }}",
                    style = nunito(13),
                    color = FinikColor.Text46,
                )
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it.take(2048); result = null },
                    enabled = !pending,
                    label = { Text("IP или адрес сервера") },
                    placeholder = { Text("192.168.1.10:8000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    singleLine = true,
                    isError = result?.first == false,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Можно указать IP, IP:порт или полный адрес. Для IP без порта используем 8000.",
                    style = nunito(13),
                    color = FinikColor.Text46,
                )
                OutlineButton(
                    text = "Эмулятор",
                    enabled = !pending,
                    onClick = { draft = "http://10.0.2.2:8000/"; result = null },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "На телефоне подключись к той же Wi-Fi сети, что и компьютер с запущенным сервером. Используй IP этого компьютера.",
                    style = nunito(13),
                    color = FinikColor.Text46,
                )
                Text(
                    text = "Если проверка не пройдёт, текущий сервер и профиль сохранятся.",
                    style = nunito(13),
                    color = FinikColor.Text46,
                )
                if (pending) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = FinikColor.Green)
                    Text("Проверяем соединение…", style = nunito(13))
                }
                result?.let { (success, message) ->
                    Text(
                        text = message,
                        style = nunito(14),
                        color = if (success) FinikColor.GreenInk34 else FinikColor.RedReset,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = draft.isNotBlank() && !busy && !pending,
                onClick = {
                    pending = true
                    result = null
                    onConnect(draft.trim()) { success, message ->
                        result = success to message
                        pending = false
                    }
                },
            ) { Text("Проверить и подключиться") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !pending) { Text("Закрыть") }
        },
    )
}
