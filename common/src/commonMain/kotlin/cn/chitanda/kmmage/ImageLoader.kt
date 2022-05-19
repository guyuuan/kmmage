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

    suspend fun execute(request: ImageRequest): ImageResult

    fun newBuilder(): Builder
    class Builder internal constructor() {
        internal var defaults: DefaultRequestOptions = DefaultRequestOptions()
        internal var memoryCache: Lazy<MemoryCache?>? = null
        internal var diskCache: Lazy<DiskCache?>? = null
        internal var httpClientFactoryLazy: Lazy<HttpClient>? = null
        internal var eventListenerFactory: EventListener.Factory? = null
        internal var componentRegistry: ComponentRegistry? = null
        internal var options: ImageLoaderOptions = ImageLoaderOptions()
        internal var context: Any? = null

        internal constructor(imageLoader: RealImageLoader) : this() {
            defaults = imageLoader.defaults
            memoryCache = imageLoader.memoryCacheLazy
            diskCache = imageLoader.diskCacheLazy
            httpClientFactoryLazy = imageLoader.httpClientFactoryLazy
            eventListenerFactory = imageLoader.eventListenerFactory
            componentRegistry = imageLoader.componentRegistry
            options = imageLoader.options
        }

    }

}

expect fun ImageLoader.Builder.build():ImageLoader