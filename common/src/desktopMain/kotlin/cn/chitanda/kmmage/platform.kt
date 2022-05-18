package cn.chitanda.kmmage

import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okio.BufferedSource
import org.jetbrains.skia.Image

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

internal actual suspend fun BufferedSource.toImageBitmap() = runCatching {
    Image.makeFromEncoded(readByteArray()).toComposeImageBitmap()
}.getOrElse {
    throw it
}


internal actual val PlatformMainDispatcher: CoroutineDispatcher = Dispatchers.Default