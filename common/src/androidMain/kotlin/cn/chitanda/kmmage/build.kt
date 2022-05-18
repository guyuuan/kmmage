package cn.chitanda.kmmage

import android.content.Context
import cn.chitanda.kmmage.disk.SingletonDiskCache
import cn.chitanda.kmmage.memory.MemoryCache
import io.ktor.client.HttpClient

actual fun ImageLoader.Builder.build(): ImageLoader {
    check(context == null) {
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