package cn.chitanda.kmmage.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import cn.chitanda.kmmage.ImageLoader

/**
 * @author: Chen
 * @createTime: 2022/5/18 16:24
 * @description:
 **/

 val LocalImageLoader = ImageLoaderProvidableCompositionLocal()

@JvmInline
value class ImageLoaderProvidableCompositionLocal internal constructor(
    private val delegate: ProvidableCompositionLocal<ImageLoader?> = staticCompositionLocalOf { null }
) {

    val current: ImageLoader
        @Composable
        @ReadOnlyComposable
        get() = delegate.current ?: getImageLoader()

    @Deprecated("Implement `ImageLoaderFactory` in your `android.app.Application` class.")
    infix fun provides(value: ImageLoader) = delegate provides value
}

@Composable
@ReadOnlyComposable
internal expect fun getImageLoader():ImageLoader