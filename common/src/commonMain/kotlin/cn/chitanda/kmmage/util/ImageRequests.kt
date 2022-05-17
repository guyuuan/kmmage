package cn.chitanda.kmmage.util

import cn.chitanda.kmmage.request.DefaultSizeResolver
import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.size.Precision

/**
 * @author: Chen
 * @createTime: 2022/5/13 16:39
 * @description:
 **/
/**
 * Return 'true' if the request does not require the output image's size to match the
 * requested dimensions exactly.
 */
internal val ImageRequest.allowInexactSize: Boolean
    get() = when (precision) {
        Precision.EXACT -> false
        Precision.INEXACT -> true
        Precision.AUTOMATIC -> run {
            // If we haven't explicitly set a size and fell back to the default size resolver,
            // always allow inexact size.
            if (sizeResolver == DefaultSizeResolver) {
                return@run true
            }

            return@run false
        }
    }

internal expect suspend fun ImageRequest.awaitStart()