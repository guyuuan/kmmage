package cn.chitanda.kmmage.request

import androidx.compose.ui.graphics.ImageBitmapConfig
import cn.chitanda.kmmage.size.Precision
import cn.chitanda.kmmage.size.Scale
import cn.chitanda.kmmage.size.SizeResolver
import cn.chitanda.kmmage.transition.Transition
import kotlinx.coroutines.CoroutineDispatcher

/**
 * @author: Chen
 * @createTime: 2022/5/19 16:23
 * @description:
 **/

data class RawRequestOptions(
    val sizeResolver: SizeResolver?,
    val scale: Scale?,
    val interceptorDispatcher: CoroutineDispatcher?=null,
    val fetcherDispatcher: CoroutineDispatcher?=null,
    val decoderDispatcher: CoroutineDispatcher? =null,
    val transformationDispatcher: CoroutineDispatcher?=null,
    val transitionFactory: Transition.Factory?,
    val precision: Precision?,
    val bitmapConfig: ImageBitmapConfig?,
    val allowHardware: Boolean?,
    val allowRgb565: Boolean?,
    val memoryCachePolicy: CachePolicy?,
    val diskCachePolicy: CachePolicy?,
    val networkCachePolicy: CachePolicy?,
)