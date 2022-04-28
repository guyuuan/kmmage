package cn.chitanda.kmmage

import cn.chitanda.common.KtorEngine
import cn.chitanda.kmmage.disk.SingletonDiskCache
import cn.chitanda.kmmage.fetch.HttpUrlFetcher
import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.request.ImageResult
import cn.chitanda.kmmage.request.Options
import io.ktor.client.HttpClient

/**
 * @author: Chen
 * @createTime: 2022/3/14 18:11
 * @description:
 **/
interface ImageLoader {
    suspend fun request(request: ImageRequest, options: Options): ImageResult

    class Builder {

        fun build(): ImageLoader = RealImageLoader(
            HttpUrlFetcher.Factory(
                HttpClient(KtorEngine),
                lazy { SingletonDiskCache.get() }, false
            )
        )
    }
}