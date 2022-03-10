package cn.chitanda.common

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

actual fun getPlatformName(): String {
    return "Android"
}

internal actual val KtorEngine: HttpClientEngineFactory<HttpClientEngineConfig> = CIO

internal actual fun ByteArray.toImageBitmap(): ImageBitmap {
   return BitmapFactory.decodeByteArray(this, 0, size).asImageBitmap()
}