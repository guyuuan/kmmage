package cn.chitanda.kmmage.intercept

import cn.chitanda.kmmage.EventListener
import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.request.ImageResult
import cn.chitanda.kmmage.request.NullRequestData
import cn.chitanda.kmmage.size.Size

/**
 * @author: Chen
 * @createTime: 2022/5/13 10:12
 * @description:
 **/
internal class RealInterceptorChain(
    val initialRequest: ImageRequest,
    val interceptors: List<Interceptor>,
    val index: Int,
    override val size: Size,
    override val request: ImageRequest,
    val eventListener: EventListener,
    val isPlaceholderCached: Boolean
) : Interceptor.Chain {
    override fun withSize(size: Size): Interceptor.Chain = copy(size = size)

    override suspend fun proceed(request: ImageRequest): ImageResult {
        if (index > 0) checkRequest(request, interceptors[index - 1])
        val interceptor = interceptors[index]
        val next = copy(index + 1, request)
        val result = interceptor.intercept(next)
        checkRequest(result.request, interceptor)
        return result
    }

    private fun checkRequest(request: ImageRequest, interceptor: Interceptor) {
        check(request.data !== NullRequestData) {
            "Interceptor '$interceptor' cannot set the request's data to null."
        }
        check(request.target === initialRequest.target) {
            "Interceptor '$interceptor' cannot modify the request's target."
        }
        check(request.sizeResolver === initialRequest.sizeResolver) {
            "Interceptor '$interceptor' cannot modify the request's size resolver. " +
                    "Use `Interceptor.Chain.withSize` instead."
        }
    }

    private fun copy(
        index: Int = this.index,
        request: ImageRequest = this.request,
        size: Size = this.size
    ) = RealInterceptorChain(
        initialRequest,
        interceptors,
        index,
        size,
        request,
        eventListener,
        isPlaceholderCached
    )
}