package cn.chitanda.kmmage.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import cn.chitanda.kmmage.request.ImageRequest

/**
 * @author: Chen
 * @createTime: 2022/5/19 16:53
 * @description:
 **/

@Composable
@ReadOnlyComposable
internal actual fun requestOf(data: Any?): ImageRequest {
    return if (data is ImageRequest) {
        data
    } else {
        ImageRequestBuilder().data(data).build()
    }
}

@ReadOnlyComposable
@Composable
actual fun ImageRequestBuilder(): ImageRequest.Builder=  ImageRequest.Builder(Unit)