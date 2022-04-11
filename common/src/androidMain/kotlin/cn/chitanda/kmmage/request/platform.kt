package cn.chitanda.kmmage.request

import android.content.res.Resources
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmapConfig
import cn.chitanda.kmmage.size.Dimension
import cn.chitanda.kmmage.size.Size
import cn.chitanda.kmmage.size.SizeResolver
import kotlin.math.max

internal actual val DEFAULT_REQUEST_OPTION: DefaultRequestOptions =
    DefaultRequestOptions(bitmapConfig = if (Build.VERSION.SDK_INT >= 26) ImageBitmapConfig.Gpu else ImageBitmapConfig.Argb8888)
internal actual val DefaultSizeResolver: SizeResolver = AndroidSizeResolver()

private class AndroidSizeResolver : SizeResolver {

    override suspend fun size(): Size {
        val metrics = Resources.getSystem().displayMetrics
        val maxDimension = Dimension(max(metrics.widthPixels, metrics.heightPixels))
        return Size(maxDimension, maxDimension)
    }

}