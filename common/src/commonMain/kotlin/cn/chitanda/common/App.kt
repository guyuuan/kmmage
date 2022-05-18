package cn.chitanda.common


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cn.chitanda.kmmage.ImageLoader
import cn.chitanda.kmmage.build
import cn.chitanda.kmmage.getPlatformName
import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.target.Target


@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()
    var text by remember { mutableStateOf("Hello, World!") }
//    var html by remember { mutableStateOf("") }
    var image by remember { mutableStateOf<ImageBitmap?>(null) }
    val imageLoader = remember { ImageLoader.Builder().build() }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column (horizontalAlignment = Alignment.CenterHorizontally){
            Box(modifier = Modifier.weight(1f)){
                image?.let {
                    Image(
                        bitmap = it,
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Text(
                "tmpdir = ${System.getProperty("java.io.tmpdir")}kmmage/image_cache/ ",
                modifier = Modifier.padding(vertical = 20.dp)
            )
        }
        var flag by remember { mutableStateOf(true) }
        Button(onClick = {
            text = "Hello, ${getPlatformName()}"
            imageLoader.enqueue(
                ImageRequest.Builder(Unit)
                    .data(if (flag) "https://cdn.pixabay.com/photo/2022/03/01/20/58/peace-genius-7042013_1280.jpg" else "https://cdn.pixabay.com/photo/2022/05/11/15/53/flower-7189649_1280.jpg")
                    .target(object : Target {
                        override fun onStart(placeholder: ImageBitmap?) {
                        }

                        override fun onError(error: ImageBitmap?) {
                        }
                        override fun onSuccess(result: ImageBitmap) {
                            flag =!flag
                            image = result
                        }
                    }).build()
            )
        }) {
            Text(text)
        }
    }
}
