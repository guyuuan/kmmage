package cn.chitanda.kmmage

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
    return "Android"
}

internal actual val KtorEngine: HttpClientEngineFactory<HttpClientEngineConfig> = CIO

internal actual suspend fun BufferedSource.toImageBitmap() = runCatching {
    BitmapFactory.decodeStream(peek().inputStream()).asImageBitmap()
}.getOrElse { throw it }


internal actual val PlatformMainDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate