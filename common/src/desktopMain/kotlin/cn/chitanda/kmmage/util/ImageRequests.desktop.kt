package cn.chitanda.kmmage.util

import cn.chitanda.kmmage.request.ImageRequest

internal actual suspend fun ImageRequest.awaitStart() {
}

fun ImageRequestBuilder() = ImageRequest.Builder(Unit)