package cn.chitanda.kmmage

import cn.chitanda.common.toImageBitmap
import cn.chitanda.kmmage.fetch.Fetcher.Factory
import cn.chitanda.kmmage.fetch.SourceResult
import cn.chitanda.kmmage.request.ErrorResult
import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.request.ImageResult
import cn.chitanda.kmmage.request.Options
import cn.chitanda.kmmage.request.SuccessResult
import io.ktor.http.Url

/**
 * @author: Chen
 * @createTime: 2022/4/28 16:52
 * @description:
 **/
class RealImageLoader(private val fetcherFactory: Factory<Url>) : ImageLoader {
    override suspend fun request(request: ImageRequest, options: Options): ImageResult {
       val fetchResult = fetcherFactory.create(data = Url(request.data.toString()), options = options, this)!!
            .fetch()
        return if (fetchResult==null){
            ErrorResult(null,request,Exception(""))
        }else{
            val result = fetchResult as SourceResult
            val bitmap = result.source.source().readByteArray().toImageBitmap()
            SuccessResult(bitmap,request,result.dataSource)
        }
    }
}