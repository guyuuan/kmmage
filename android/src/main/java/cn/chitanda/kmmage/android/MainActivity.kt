package cn.chitanda.kmmage.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import cn.chitanda.kmmage.compose.AsyncImage
import cn.chitanda.kmmage.compose.LocalImageLoader
import cn.chitanda.kmmage.getPlatformName

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var text by remember { mutableStateOf("Hello, World!") }
                var image by remember { mutableStateOf<ImageBitmap?>(null) }
                val imageLoader = LocalImageLoader.current
                var flag by remember { mutableStateOf(true) }

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.weight(1f)) {
                            AsyncImage(
                                data = if (flag) "https://cdn.pixabay.com/photo/2022/03/01/20/58/peace-genius-7042013_1280.jpg"
                                else "https://cdn.pixabay.com/photo/2022/05/11/15/53/flower-7189649_1280.jpg",
                                modifier = Modifier.fillMaxSize()
                                    .background(color= Color.DarkGray)
                                , contentDescription = ""
                            )
                        }
                        Text(
                            "tmpdir = ${System.getProperty("java.io.tmpdir")}/kmmage/image_cache/ ",
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                    Button(onClick = {
                        text = "Hello, ${getPlatformName()}"
                        flag=!flag
                }) {
                    Text(text)
                }
            }
            }
        }
    }
}