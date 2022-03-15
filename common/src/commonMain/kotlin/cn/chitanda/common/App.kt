package cn.chitanda.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import cn.chitanda.common.http.Api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.FileSystem

@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()
    var text by remember { mutableStateOf("Hello, World!") }
//    var html by remember { mutableStateOf("") }
    var image by remember { mutableStateOf<ImageBitmap?>(null) }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        image?.let { Image(bitmap = it, contentScale = ContentScale.Crop,contentDescription = null, modifier = Modifier.fillMaxSize(),) }
        Text("tmpdir = ${System.getProperty("java.io.tmpdir")} ",modifier = Modifier.align(Alignment.BottomCenter))
        Button(onClick = {
            text = "Hello, ${getPlatformName()}"
            coroutineScope.launch(Dispatchers.IO) {
//                html = Api.getWebHTML("https://www.bing.com")
                image = Api.getImageBitmap("https://cdn.pixabay.com/photo/2022/03/01/20/58/peace-genius-7042013_1280.jpg")
            }
        }) {
            Text(text)
        }
    }
}
