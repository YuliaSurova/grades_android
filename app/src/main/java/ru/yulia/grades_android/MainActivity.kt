package ru.yulia.grades_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.yulia.grades_android.di.ServiceLocator
import ru.yulia.grades_android.ui.GradesApp
import ru.yulia.grades_android.ui.GradesViewModel
import ru.yulia.grades_android.ui.PreviewGradesContent
import ru.yulia.grades_android.ui.theme.GradesTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GradesTheme {
                val viewModel: GradesViewModel = viewModel(factory = ServiceLocator.provideGradesViewModelFactory())
                GradesApp(viewModel = viewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GradesPreview() {
    GradesTheme {
        PreviewGradesContent()
    }
}
