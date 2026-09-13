package dev.bober.finik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.ui.FinikApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinikTheme {
                FinikApp()
            }
        }
    }
}
