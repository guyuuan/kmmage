package cn.chitanda.kmmage.compose

import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import cn.chitanda.kmmage.ImageLoader
import cn.chitanda.kmmage.request.ErrorResult
import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.request.SuccessResult
import cn.chitanda.kmmage.size.Dimension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.roundToInt
import cn.chitanda.kmmage.size.Size as KmmageSize

/**
 * @author: Chen
 * @createTime: 2022/5/18 17:39
 * @description:
 **/
class AsyncImagePainter(request: ImageRequest, imageLoader: ImageLoader) : Painter(
), RememberObserver {
    private var coroutineScope: CoroutineScope? = null
    private val drawSize = MutableStateFlow(Size.Zero)
    private var painter: Painter? by mutableStateOf(null)
    private var alpha: Float by mutableStateOf(DefaultAlpha)
    private var colorFilter: ColorFilter? by mutableStateOf(null)
    private var state: State = State.Empty

    override val intrinsicSize: Size
        get() = TODO("Not yet implemented")

    override fun DrawScope.onDraw() {
        TODO("Not yet implemented")
    }

    override fun onAbandoned() {
        TODO("Not yet implemented")
    }

    override fun onForgotten() {
        TODO("Not yet implemented")
    }

    override fun onRemembered() {
        TODO("Not yet implemented")
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