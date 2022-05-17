package cn.chitanda.kmmage

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import net.coobird.thumbnailator.Thumbnails
import okio.BufferedSource

/**
 * @author: Chen
 * @createTime: 2022/5/13 09:57
 * @description:
 **/
internal actual fun buildComponentRegistry(componentRegistry: ComponentRegistry): ComponentRegistry {
    return componentRegistry.newBuilder().build()
}

actual fun getPlatformName(): String {
    return System.getProperty("os.name") ?: "unknown"
}

internal actual val KtorEngine: HttpClientEngineFactory<HttpClientEngineConfig> = CIO

internal actual fun BufferedSource.toImageBitmap(): ImageBitmap {
    return Thumbnails.of(inputStream()).size(100,100).asBufferedImage().toComposeImageBitmap()
}