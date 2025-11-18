package ru.yulia.grades_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoneyManagerApp()
        }
    }
}

@Composable
private fun MoneyManagerApp() {}

@Preview(showBackground = true)
@Composable
private fun MoneyManagerPreview() {
    MoneyManagerApp()
}

