package dev.bober.finik.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.designsystem.theme.unbounded

@Composable
fun FinikConfirmSheet(
    title: String,
    body: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String = "Не сейчас",
    warning: Boolean = false,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FinikColor.Scrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
            .navigationBarsPadding()
            .padding(14.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(FinikColor.Surface)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = title, style = unbounded(19, lineHeight = 1.25), color = FinikColor.Ink)
            Text(
                text = body,
                style = nunito(14, FontWeight.SemiBold, lineHeight = 1.45),
                color = if (warning) FinikColor.Red else FinikColor.Text42,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                PrimaryButton(
                    text = confirmText,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    height = 48.dp,
                    radius = 12.dp,
                    textStyle = nunito(15),
                    containerColor = if (warning) FinikColor.Red else FinikColor.Green,
                    contentColor = if (warning) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
                )
                OutlineButton(
                    text = cancelText,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    height = 48.dp,
                    radius = 12.dp,
                )
            }
        }
    }
    }
}
