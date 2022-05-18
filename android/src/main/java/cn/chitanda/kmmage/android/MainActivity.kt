package cn.chitanda.kmmage.android

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cn.chitanda.kmmage.compose.LocalImageLoader
import cn.chitanda.kmmage.getPlatformName
import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.target.Target

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var text by remember { mutableStateOf("Hello, World!") }
                var image by remember { mutableStateOf<ImageBitmap?>(null) }
                val imageLoader = LocalImageLoader.current
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.weight(1f)) {
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
                            "tmpdir = ${System.getProperty("java.io.tmpdir")}/kmmage/image_cache/ ",
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                    var flag by remember { mutableStateOf(true) }
                    Button(onClick = {
                        text = "Hello, ${getPlatformName()}"
                        imageLoader.enqueue(
                            ImageRequest.Builder(this@MainActivity)
                                .data(
                                    if (flag) "https://cdn.pixabay.com/photo/2022/03/01/20/58/peace-genius-7042013_1280.jpg"
                                    else "https://cdn.pixabay.com/photo/2022/05/11/15/53/flower-7189649_1280.jpg"
                                )
                                .target(object : Target {
                                    override fun onStart(placeholder: ImageBitmap?) {
                                    }

                                    override fun onError(error: ImageBitmap?) {
                                        Log.d(TAG, "onError: ")
                                    }

                                    override fun onSuccess(result: ImageBitmap) {
                                        image = result
                                        flag = !flag
                                    }
                                }).build()
                        )
                }) {
                    Text(text)
                }
            }
            }
        }
    }
}