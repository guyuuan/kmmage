package cn.chitanda.common

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import okio.BufferedSource

actual fun getPlatformName(): String {
    return "Android"
}

internal actual val KtorEngine: HttpClientEngineFactory<HttpClientEngineConfig> = CIO

internal actual fun BufferedSource.toImageBitmap(): ImageBitmap {
   return BitmapFactory.decodeStream(buffer.inputStream()).asImageBitmap()
}