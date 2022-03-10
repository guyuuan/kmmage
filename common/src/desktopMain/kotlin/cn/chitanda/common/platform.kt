package cn.chitanda.common

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import org.jetbrains.skia.Image

actual fun getPlatformName(): String {
    return System.getProperty("os.name") ?: "unknown"
}

internal actual val KtorEngine: HttpClientEngineFactory<HttpClientEngineConfig> = CIO

internal actual fun ByteArray.toImageBitmap(): ImageBitmap {
    return Image.makeFromEncoded(this).toComposeImageBitmap()
}