package dev.bober.finik.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.nunito

/**
 * Всплывающая подсказка макета: тёмно-зелёная (или красная для предупреждений),
 * `radius 14; padding 13px 15px; 700 14px/1.4`. Показ управляется логикой — появится позже.
 */
@Composable
fun FinikToast(
    text: String,
    modifier: Modifier = Modifier,
    warning: Boolean = false,
) {
    Text(
        text = text,
        style = nunito(14, lineHeight = 1.4),
        color = Color.White,
        modifier = modifier
            .fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(14.dp), spotColor = Color.Black.copy(alpha = 0.45f))
            .background(if (warning) FinikColor.Red else FinikColor.GreenToast, RoundedCornerShape(14.dp))
            .padding(horizontal = 15.dp, vertical = 13.dp),
    )
}

@Preview(showBackground = true, widthDp = 388)
@Composable
private fun FinikToastPreview() {
    FinikTheme {
        FinikToast(text = "Отложено 5. До цели 83.", modifier = Modifier.padding(12.dp))
    }
}
