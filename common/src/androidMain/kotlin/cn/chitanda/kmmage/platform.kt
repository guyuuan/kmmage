package cn.chitanda.kmmage

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import cn.chitanda.kmmage.decode.BitmapFactoryDecoder
import cn.chitanda.kmmage.disk.SingletonDiskCache
import cn.chitanda.kmmage.memory.MemoryCache
import cn.chitanda.kmmage.util.addFirst
import io.ktor.client.HttpClient
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
internal actual fun ComponentRegistry.addPlatformComponentRegistry(): ComponentRegistry {
    return newBuilder().addFirst(BitmapFactoryDecoder.Factory()).build()
}

actual fun getPlatformName(): String {
    return "Android"
}

internal actual val KtorEngine: HttpClientEngineFactory<HttpClientEngineConfig> = CIO

internal actual suspend fun BufferedSource.toImageBitmap() = runCatching {
    BitmapFactory.decodeStream(peek().inputStream()).asImageBitmap()
}.getOrElse { throw it }


internal actual val PlatformMainDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate


actual fun ImageLoader.Builder.build(): ImageLoader {
    check(context != null) {
        "Android Context cannot be null"
    }
    check(context is Context) {
        "ImageLoader context must is android.content.Context"
    }
    return RealImageLoader(
        defaults = defaults,
        memoryCacheLazy = memoryCache ?: lazy { MemoryCache.Builder().build() },
        diskCacheLazy = diskCache ?: lazy { SingletonDiskCache.get() },
        httpClientFactoryLazy = httpClientFactoryLazy ?: lazy { HttpClient() },
        eventListenerFactory = eventListenerFactory ?: EventListener.Factory.NONE,
        componentRegistry = componentRegistry ?: ComponentRegistry(),
        options = options,
        context = context as Context
    )
}