package cn.chitanda.common

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import net.coobird.thumbnailator.Thumbnails
import okio.BufferedSource
import org.jetbrains.skia.Image
import java.io.InputStream

actual fun getPlatformName(): String {
    return System.getProperty("os.name") ?: "unknown"
}

internal actual val KtorEngine: HttpClientEngineFactory<HttpClientEngineConfig> = CIO

internal actual fun BufferedSource.toImageBitmap(): ImageBitmap {
    return Thumbnails.of(inputStream()).size(100,100).asBufferedImage().toComposeImageBitmap()
}