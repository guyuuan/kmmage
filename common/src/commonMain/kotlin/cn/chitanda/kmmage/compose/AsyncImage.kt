package cn.chitanda.kmmage.compose

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import cn.chitanda.kmmage.ImageLoader
import cn.chitanda.kmmage.compose.State.Companion.DefaultTransform
import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.size.Dimension
import cn.chitanda.kmmage.size.SizeResolver
import cn.chitanda.kmmage.util.requestOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import cn.chitanda.kmmage.size.Size as KmmageSize

/**
 * @author: Chen
 * @createTime: 2022/5/18 17:06
 * @description:
 **/
@Composable
fun AsyncImage(
    data: Any?,
    contentDescription: String?,
    imageLoader: ImageLoader = LocalImageLoader.current,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
    error: Painter? = null,
    fallback: Painter? = error,
    onLoading: ((State.Loading) -> Unit)? = null,
    onSuccess: ((State.Success) -> Unit)? = null,
    onError: ((State.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality,
) = AsyncImage(
    data = data,
    contentDescription = contentDescription,
    imageLoader = imageLoader,
    modifier = modifier,
    transform = transformOf(placeholder, error, fallback),
    onState = onStateOf(onLoading, onSuccess, onError),
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
    filterQuality = filterQuality
)

@Composable
fun AsyncImage(
    data: Any?,
    contentDescription: String?,
    imageLoader: ImageLoader= LocalImageLoader.current,
    modifier: Modifier,
    transform: (State) -> State = DefaultTransform,
    onState: ((State) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit, alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality
) {
    val request = updateRequest(requestOf(data), contentScale)
    val painter = rememberAsyncImagePainter(
        request, imageLoader, transform, onState, contentScale, filterQuality
    )
    val sizeResolver = request.sizeResolver
    Content(
        modifier = if (sizeResolver is ConstraintsSizeResolver) {
            modifier.then(sizeResolver)
        } else {
            modifier
        },
        painter = painter,
        contentDescription = contentDescription,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}

@Composable
internal fun Content(
    modifier: Modifier,
    painter: Painter,
    contentDescription: String?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?
) = Layout(
    modifier = modifier
        .contentDescription(contentDescription)
        .clipToBounds()
        .then(
            ContentPainterModifier(
                painter = painter,
                alignment = alignment,
                contentScale = contentScale,
                alpha = alpha,
                colorFilter = colorFilter
            )
        ),
    measurePolicy = { _, constraints ->
        layout(constraints.minWidth, constraints.minHeight) {}
    }
)


@Stable
private fun Modifier.contentDescription(contentDescription: String?): Modifier {
    return if (contentDescription != null) {
        semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        this
    }
}

@Composable
internal fun updateRequest(request: ImageRequest, contentScale: ContentScale): ImageRequest {
    return if (request.raw.sizeResolver == null) {
        val sizeResolver = if (contentScale == ContentScale.None) {
            SizeResolver(KmmageSize.ORIGINAL)
        } else {
            remember { ConstraintsSizeResolver() }
        }
        request.newBuilder().size(sizeResolver).build()
    } else {
        request
    }
}

internal class ConstraintsSizeResolver() : SizeResolver, LayoutModifier {
    private val _constraints = MutableStateFlow(ZeroConstraints)
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        _constraints.value = constraints
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }

    override suspend fun size() = _constraints.mapNotNull(Constraints::toSizeOrNull).first()

    fun setConstraints(constraints: Constraints) {
        _constraints.value = constraints
    }
}

@Stable
private fun Constraints.toSizeOrNull() = when {
    isZero -> null
    else -> KmmageSize(
        width = if (hasBoundedWidth) Dimension(maxWidth) else Dimension.Undefined,
        height = if (hasBoundedHeight) Dimension(maxHeight) else Dimension.Undefined
    )
}

internal val ZeroConstraints = Constraints.fixed(0, 0)