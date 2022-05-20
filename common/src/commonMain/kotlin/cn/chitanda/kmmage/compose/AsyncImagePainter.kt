package cn.chitanda.kmmage.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import cn.chitanda.kmmage.ImageLoader
import cn.chitanda.kmmage.PlatformMainDispatcher
import cn.chitanda.kmmage.compose.State.Companion.DefaultTransform
import cn.chitanda.kmmage.request.ErrorResult
import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.request.ImageResult
import cn.chitanda.kmmage.request.NullRequestDataException
import cn.chitanda.kmmage.request.SuccessResult
import cn.chitanda.kmmage.size.Dimension
import cn.chitanda.kmmage.size.Precision
import cn.chitanda.kmmage.util.requestOf
import cn.chitanda.kmmage.util.toScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import cn.chitanda.kmmage.size.Size as KmmageSize

/**
 * @author: Chen
 * @createTime: 2022/5/18 17:39
 * @description:
 **/

@Composable
fun rememberAsyncImagePainter(
    data: Any?,
    imageLoader: ImageLoader,
    placeholder: Painter? = null,
    error: Painter? = null,
    fallback: Painter? = error,
    onLoading: ((State.Loading) -> Unit)? = null,
    onSuccess: ((State.Success) -> Unit)? = null,
    onError: ((State.Error) -> Unit)? = null,
    contentScale: ContentScale = ContentScale.Fit,
    filterQuality: FilterQuality = DefaultFilterQuality,
) = rememberAsyncImagePainter(
    data  = data,
    imageLoader = imageLoader,
    transform = transformOf(placeholder, error, fallback),
    onState = onStateOf(onLoading, onSuccess, onError),
    contentScale = contentScale,
    filterQuality = filterQuality,
)

@Composable
fun rememberAsyncImagePainter(
    data: Any?, imageLoader: ImageLoader,
    transform: (State) -> State = DefaultTransform,
    onState: ((State) -> Unit)? = null,
    contentScale: ContentScale = ContentScale.Fit,
    filterQuality: FilterQuality = DefaultFilterQuality,
): AsyncImagePainter {
    val request = requestOf(data)
    validateRequest(request)
    val painter = remember { AsyncImagePainter(request, imageLoader) }
    painter.transform = transform
    painter.onState = onState
    painter.contentScale = contentScale
    painter.filterQuality = filterQuality
    painter.isPreview = LocalInspectionMode.current
    painter.imageLoader = imageLoader
    painter.request = request
    painter.onRemembered()
    return painter
}

@Stable
internal fun transformOf(
    placeholder: Painter?,
    error: Painter?,
    fallback: Painter?,
): (State) -> State {
    return if (placeholder != null || error != null || fallback != null) {
        { state ->
            when (state) {
                is State.Loading -> {
                    if (placeholder != null) state.copy(painter = placeholder) else state
                }
                is State.Error -> if (state.result.throwable is NullRequestDataException) {
                    if (fallback != null) state.copy(painter = fallback) else state
                } else {
                    if (error != null) state.copy(painter = error) else state
                }
                else -> state
            }
        }
    } else {
        DefaultTransform
    }
}

@Stable
internal fun onStateOf(
    onLoading: ((State.Loading) -> Unit)?,
    onSuccess: ((State.Success) -> Unit)?,
    onError: ((State.Error) -> Unit)?,
): ((State) -> Unit)? {
    return if (onLoading != null || onSuccess != null || onError != null) {
        { state ->
            when (state) {
                is State.Loading -> onLoading?.invoke(state)
                is State.Success -> onSuccess?.invoke(state)
                is State.Error -> onError?.invoke(state)
                is State.Empty -> {}
            }
        }
    } else {
        null
    }
}
class AsyncImagePainter(request: ImageRequest, imageLoader: ImageLoader) : Painter(
), RememberObserver {
    private var coroutineScope: CoroutineScope? = null
    private val drawSize = MutableStateFlow(Size.Zero)
    private var painter: Painter? by mutableStateOf(null)
    private var alpha: Float by mutableStateOf(DefaultAlpha)
    private var colorFilter: ColorFilter? by mutableStateOf(null)
    private var _state: State = State.Empty
        set(value) {
            field = value
            state = value
        }
    private var _painter: Painter? = null
        set(value) {
            field = value
            painter = value
        }

    internal var transform = DefaultTransform
    internal var onState: ((State) -> Unit)? = null
    internal var contentScale = ContentScale.Fit
    internal var filterQuality = DefaultFilterQuality
    internal var isPreview = false

    var state: State by mutableStateOf(State.Empty)
        private set

    var request: ImageRequest by mutableStateOf(request)
        internal set
    var imageLoader: ImageLoader by mutableStateOf(imageLoader)
        internal set


    override val intrinsicSize: Size
        get() = painter?.intrinsicSize ?: Size.Unspecified

    override fun DrawScope.onDraw() {
        drawSize.value = size
        painter?.apply { draw(size, alpha, colorFilter) }
    }

    override fun onAbandoned() {
        clear()
        (_painter as? RememberObserver)?.onAbandoned()
    }

    override fun onForgotten() {
        clear()
        (_painter as? RememberObserver)?.onForgotten()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onRemembered() {
        if (coroutineScope != null) return
        val scope = CoroutineScope(SupervisorJob() + PlatformMainDispatcher)
        coroutineScope = scope
        (_painter as? RememberObserver)?.onRemembered()

        if (isPreview) {
            val request = request.newBuilder().defaults(imageLoader.defaults).build()
            updateState(State.Loading(request.placeholder?.toPainter()))
            return
        }

        scope.launch {
            snapshotFlow { request }.mapLatest {
                imageLoader.execute(updateRequest(request)).toState().also { println("ASYNC STATE $it") }
            }.collect(::updateState)
        }
    }

    private fun updateState(input: State) {
        val previous = _state
        val current = transform(input)
        _state = current
        _painter = maybeNewCrossfadePainter(previous, current) ?: current.painter
        if (coroutineScope != null && previous.painter !== current.painter) {
            (previous.painter as? RememberObserver)?.onForgotten()
            (current.painter as? RememberObserver)?.onRemembered()
        }
        onState?.invoke(current)
    }

    private fun updateRequest(request: ImageRequest): ImageRequest {
        return request.newBuilder()
            .target(onStart = { placeholder -> updateState(State.Loading(placeholder?.toPainter())) })
            .apply {
                if (request.raw.sizeResolver == null) {
                    size { drawSize.mapNotNull { it.toSizeOrNull() }.first() }
                }
                if (request.raw.scale == null) {
                    scale(contentScale.toScale())
                }
                if (request.raw.precision != Precision.EXACT) {
                    precision(Precision.INEXACT)
                }
            }
            .build()
    }

    private fun maybeNewCrossfadePainter(previous: State, current: State): CrossfadePainter? {
        return null
    }

    private fun clear() {
        coroutineScope?.cancel()
        coroutineScope = null
    }

    private fun ImageBitmap.toPainter(): Painter =
        BitmapPainter(this, filterQuality = filterQuality)

    private fun ImageResult.toState() = when (this) {
        is SuccessResult -> State.Success(bitmap.toPainter(), this)
        is ErrorResult -> State.Error(bitmap?.toPainter(), this)
    }
}

sealed class State {
    abstract val painter: Painter?

    object Empty : State() {
        override val painter: Painter?
            get() = null
    }

    data class Loading(override val painter: Painter?) : State()
    data class Success(override val painter: Painter, val result: SuccessResult) : State()
    data class Error(override val painter: Painter?, val result: ErrorResult) : State()

    companion object {
        val DefaultTransform: (State) -> State = { it }
    }
}

private fun validateRequest(request: ImageRequest) {
    when (request.data) {
        is ImageRequest.Builder -> unsupportedData(
            name = "ImageRequest.Builder",
            description = "Did you forget to call ImageRequest.Builder.build()?"
        )
        is ImageBitmap -> unsupportedData("ImageBitmap")
        is ImageVector -> unsupportedData("ImageVector")
        is Painter -> unsupportedData("Painter")
    }
    require(request.target == null) { "request.target must be null." }
}

private fun unsupportedData(
    name: String,
    description: String = "If you wish to display this $name, use androidx.compose.foundation.Image."
): Nothing = throw IllegalArgumentException("Unsupported type: $name. $description")

private val Size.isPositive get() = width >= 0.5 && height >= 0.5

private fun Size.toSizeOrNull() = when {
    isUnspecified -> KmmageSize.ORIGINAL
    isPositive -> KmmageSize(
        width = if (width.isFinite()) Dimension(width.roundToInt()) else Dimension.Undefined,
        height = if (height.isFinite()) Dimension(height.roundToInt()) else Dimension.Undefined
    )
    else -> null
}