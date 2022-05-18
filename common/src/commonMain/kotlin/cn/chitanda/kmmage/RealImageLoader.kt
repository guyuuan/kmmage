package cn.chitanda.kmmage

import cn.chitanda.kmmage.decode.BaseBitmapDecoder
import cn.chitanda.kmmage.disk.DiskCache
import cn.chitanda.kmmage.fetch.HttpUrlFetcher
import cn.chitanda.kmmage.intercept.EngineInterceptor
import cn.chitanda.kmmage.intercept.RealInterceptorChain
import cn.chitanda.kmmage.map.StringMapper
import cn.chitanda.kmmage.memory.MemoryCache
import cn.chitanda.kmmage.request.DefaultRequestOptions
import cn.chitanda.kmmage.request.Disposable
import cn.chitanda.kmmage.request.ErrorResult
import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.request.ImageResult
import cn.chitanda.kmmage.request.NullRequestData
import cn.chitanda.kmmage.request.OneShotDisposable
import cn.chitanda.kmmage.request.Options
import cn.chitanda.kmmage.request.RequestService
import cn.chitanda.kmmage.request.SuccessResult
import cn.chitanda.kmmage.target.Target
import cn.chitanda.kmmage.transition.NoneTransition
import cn.chitanda.kmmage.util.ImageLoaderOptions
import cn.chitanda.kmmage.util.SystemCallbacks
import cn.chitanda.kmmage.util.awaitStart
import cn.chitanda.kmmage.util.get
import io.ktor.client.HttpClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

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
    val context: Any
) : ImageLoader {

    private val scope = CoroutineScope(SupervisorJob() + PlatformMainDispatcher +
            CoroutineExceptionHandler { _, throwable -> println(throwable) })
    override val memoryCache by memoryCacheLazy
    override val diskCache by diskCacheLazy
    override val components = componentRegistry.newBuilder()
        .add(StringMapper())
        .add(BaseBitmapDecoder.Factory())
        .add(
        HttpUrlFetcher.Factory(
            HttpClient(KtorEngine),
            diskCacheLazy,
            options.respectCacheHeaders
        )
    ).build()
    private val systemCallbacks = SystemCallbacks(this, options.networkObserverEnabled)
    private val requestService = RequestService(this, systemCallbacks)
    private val interceptors = components.interceptors +
            EngineInterceptor(this, requestService)

    override fun enqueue(request: ImageRequest): Disposable {
        val job = scope.async {
            executeMain(request, REQUEST_TYPE_ENQUEUE)
        }
        return OneShotDisposable(job)
    }

    override suspend fun execute(request: ImageRequest, options: Options): ImageResult {
        val job = scope.async {
            executeMain(request, REQUEST_TYPE_ENQUEUE)
        }
        return job.await()
    }

    override fun newBuilder() = ImageLoader.Builder(this)

    private suspend fun executeMain(initialRequest: ImageRequest, type: Int): ImageResult {
        val requestDelegate = requestService.requestDelegate(initialRequest, coroutineContext.job)
        val request = initialRequest.newBuilder().defaults(defaults).build()
        val eventListener = eventListenerFactory.create(request)
        try {
            if (request.data == NullRequestData) throw  RuntimeException("The request's data is null.")
            if (type == REQUEST_TYPE_ENQUEUE) request.awaitStart()
            val placeholder = memoryCache?.get(request.placeholderMemoryCacheKey)?.bitmap
            request.target?.onStart(placeholder)
            eventListener.onStart(request)
            request.listener?.onStart(request)
            eventListener.resolveSizeStart(request)
            val size = request.sizeResolver.size()
            eventListener.resolveSizeEnd(request, size)
            val result = withContext(Dispatchers.IO) {
                RealInterceptorChain(
                    initialRequest = request,
                    interceptors = interceptors,
                    index = 0,
                    request = request,
                    size = size,
                    eventListener = eventListener,
                    isPlaceholderCached = placeholder != null
                ).proceed(request)
            }
            when (result) {
                is SuccessResult -> onSuccess(result, request.target, eventListener)
                is ErrorResult -> onError(result, request.target, eventListener)
            }
            return result
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                onCancel(request, eventListener)
                throw throwable
            } else {
                // Create the default error result if there's an uncaught exception.
                val result = requestService.errorResult(request, throwable)
                onError(result, request.target, eventListener)
                return result
            }
        } finally {
            requestDelegate.complete()
        }
    }

    private fun onSuccess(result: SuccessResult, target: Target?, eventListener: EventListener) {
        val request = result.request
        val dataSource = result.dataSource
        transition(result, target, eventListener) { target?.onSuccess(result.bitmap) }
        eventListener.onSuccess(request, result)
        request.listener?.onSuccess(request, result)
    }

    private fun onError(result: ErrorResult, target: Target?, eventListener: EventListener) {
        val request = result.request
        transition(result, request.target, eventListener) { target?.onError(result.bitmap) }
        eventListener.onError(request, result)
        request.listener?.onError(request, result)
    }

    private fun onCancel(request: ImageRequest, eventListener: EventListener) {
        eventListener.onCancel(request)
        request.listener?.onCancel(request)
    }

    private inline fun transition(
        result: ImageResult,
        target: Target?,
        eventListener: EventListener,
        setBitmap: () -> Unit
    ) {

        val transition = result.request.transitionFactory.create(target!!, result)
        if (transition is NoneTransition) {
            setBitmap()
            return
        }

        eventListener.transitionStart(result.request, transition)
        transition.transition()
        eventListener.transitionEnd(result.request, transition)
    }

    companion object {
        private const val REQUEST_TYPE_ENQUEUE = 0
        private const val REQUEST_TYPE_EXECUTE = 1
    }
}