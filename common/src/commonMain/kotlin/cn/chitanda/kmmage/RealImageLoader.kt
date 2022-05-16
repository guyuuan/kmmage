package cn.chitanda.kmmage

import cn.chitanda.kmmage.disk.DiskCache
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
 * @createTime: 2022/4/28 16:52
 * @description:
 **/
internal class RealImageLoader(
    override val defaults: DefaultRequestOptions,
    val memoryCacheLazy: Lazy<MemoryCache?>,
    val diskCacheLazy: Lazy<DiskCache?>,
    val httpClientFactoryLazy: Lazy<HttpClient>,
    val eventListenerFactory: EventListener.Factory,
    val componentRegistry: ComponentRegistry,
    val options: ImageLoaderOptions,
) : ImageLoader {

    override val memoryCache by memoryCacheLazy
    override val diskCache by diskCacheLazy
    override fun enqueue(request: ImageRequest): Disposable {
        TODO("Not yet implemented")
    }

    override val components = componentRegistry.newBuilder().build()

    override suspend fun request(request: ImageRequest, options: Options): ImageResult {
        TODO("Not yet implemented")
    }

    override fun newBuilder() = ImageLoader.Builder(this)


}