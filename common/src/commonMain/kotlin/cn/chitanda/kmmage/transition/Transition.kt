package cn.chitanda.kmmage.transition

import cn.chitanda.kmmage.request.ImageResult
import cn.chitanda.kmmage.target.Target

fun interface Transition {

    fun transition()

    fun interface Factory {

        fun create(target: Target, result: ImageResult): Transition

    }
}