import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cn.chitanda.common.App
import cn.chitanda.kmmage.request.updateWindowBounds

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        with(window.bounds) {
            updateWindowBounds(width to height)
        }
        MaterialTheme {
            App()
        }
    }
}