package cn.chitanda.kmmage

import androidx.compose.ui.graphics.toComposeImageBitmap
import cn.chitanda.kmmage.disk.SingletonDiskCache
import cn.chitanda.kmmage.memory.MemoryCache
import io.ktor.client.HttpClient
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
internal actual fun ComponentRegistry.addPlatformComponentRegistry(): ComponentRegistry {
    return this
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

actual fun ImageLoader.Builder.build(): ImageLoader {
    return RealImageLoader(
        defaults = defaults,
        memoryCacheLazy = memoryCache ?: lazy { MemoryCache.Builder().build() },
        diskCacheLazy = diskCache ?: lazy { SingletonDiskCache.get() },
        httpClientFactoryLazy = httpClientFactoryLazy ?: lazy { HttpClient() },
        eventListenerFactory = eventListenerFactory ?: EventListener.Factory.NONE,
        componentRegistry = componentRegistry ?: ComponentRegistry(),
        options = options,
        context =  Unit
    )
}