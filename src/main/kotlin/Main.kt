
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

val viewModel = MainViewModel()
fun main() = application {
    Window(
        onCloseRequest = {
            viewModel.onDestroy()
            exitApplication()
        },
        title = "Serial Port Manager",
        state = rememberWindowState(placement = WindowPlacement.Fullscreen),
    ) {
        MaterialTheme {
            MainScreen(viewModel)
        }
    }
}
