package cn.chitanda.common


import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.unit.dp
import cn.chitanda.kmmage.compose.AsyncImage
import cn.chitanda.kmmage.disk.SingletonDiskCache
import cn.chitanda.kmmage.getPlatformName
import kotlinx.coroutines.currentCoroutineContext


@Composable
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }
    var flag by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.weight(1f)) {
                AsyncImage(
                    data = if (flag) "https://cdn.pixabay.com/photo/2022/03/01/20/58/peace-genius-7042013_1280.jpg"
                    else "https://cdn.pixabay.com/photo/2022/05/11/15/53/flower-7189649_1280.jpg",
                    modifier = Modifier.align(Alignment.Center).size(300.dp),
                    contentDescription = "", placeholder = ColorPainter(Color.DarkGray)
                )
            }
            Text(
                "tmpdir = ${SingletonDiskCache.get().directory}",
                modifier = Modifier.padding(vertical = 20.dp)
            )
        }
        Button(onClick = {
            text = "Hello, ${getPlatformName()}"
            flag = !flag
        }) {
            Text(text)
        }
    }
}
