package cn.chitanda.kmmage

import cn.chitanda.kmmage.disk.DiskCache
import cn.chitanda.kmmage.disk.SingletonDiskCache
import cn.chitanda.kmmage.memory.MemoryCache
import cn.chitanda.kmmage.request.DefaultRequestOptions
import cn.chitanda.kmmage.request.Disposable
import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.request.ImageResult
import cn.chitanda.kmmage.request.Options
import cn.chitanda.kmmage.util.ImageLoaderOptions
import io.ktor.client.HttpClient

/**
 * @author: Chen
 * @createTime: 2022/3/14 18:11
 * @description:
 **/
interface ImageLoader {
    val defaults: DefaultRequestOptions
    val components: ComponentRegistry
    val memoryCache: MemoryCache?
    val diskCache: DiskCache?

    fun enqueue(request: ImageRequest): Disposable

    suspend fun request(request: ImageRequest, options: Options): ImageResult

    fun newBuilder(): Builder
    class Builder {
        private var defaults: DefaultRequestOptions = DefaultRequestOptions()
        private var memoryCache: Lazy<MemoryCache?>? = null
        private var diskCache: Lazy<DiskCache?>? = null
        private var httpClientlFactoryLazy: Lazy<HttpClient>? = null
        private var eventListenerFactory: EventListener.Factory? = null
        private var componentRegistry: ComponentRegistry? = null
        private var options: ImageLoaderOptions = ImageLoaderOptions()

        internal constructor(imageLoader: RealImageLoader) {
            defaults = imageLoader.defaults
            memoryCache = imageLoader.memoryCacheLazy
            diskCache = imageLoader.diskCacheLazy
            httpClientlFactoryLazy = imageLoader.httpClientlFactoryLazy
            eventListenerFactory = imageLoader.eventListenerFactory
            componentRegistry = imageLoader.componentRegistry
            options = imageLoader.options
        }

        fun build(): ImageLoader = RealImageLoader(defaults = defaults,
            memoryCacheLazy = memoryCache ?: lazy { MemoryCache.Builder().build() },
            diskCacheLazy = diskCache ?: lazy { SingletonDiskCache.get() },
            httpClientlFactoryLazy = httpClientlFactoryLazy ?: lazy { HttpClient() },
            eventListenerFactory = eventListenerFactory ?: EventListener.Factory.NONE,
            componentRegistry = componentRegistry ?: ComponentRegistry(),
            options = options,)

    }
}