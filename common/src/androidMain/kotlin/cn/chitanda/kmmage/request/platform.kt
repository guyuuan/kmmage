package cn.chitanda.kmmage.request

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
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

fun ImageRequest.Builder.lifecycle(lifecycle: Lifecycle) = apply {
    this.lifecycle = lifecycle
}

internal actual fun Any.getLifecycle(): Any {
    check(this is Context) {
        "ImageRequest context must is Android Context"
    }
    var context = this
    val lifecycle: Lifecycle?
    while (true) {
        when (context) {
            is LifecycleOwner -> {
                lifecycle = context.lifecycle
                break
            }
            !is ContextWrapper -> {
                lifecycle = null
                break
            }
            else -> context = context.baseContext
        }
    }

    return lifecycle ?: GlobalLifecycle
}

internal object GlobalLifecycle : Lifecycle() {

    private val owner = LifecycleOwner { this }

    override fun addObserver(observer: LifecycleObserver) {
        require(observer is DefaultLifecycleObserver) {
            "$observer must implement androidx.lifecycle.DefaultLifecycleObserver."
        }

        // Call the lifecycle methods in order and do not hold a reference to the observer.
        observer.onCreate(owner)
        observer.onStart(owner)
        observer.onResume(owner)
    }

    override fun removeObserver(observer: LifecycleObserver) {}

    override fun getCurrentState() = State.RESUMED

    override fun toString() = "coil.request.GlobalLifecycle"
}
