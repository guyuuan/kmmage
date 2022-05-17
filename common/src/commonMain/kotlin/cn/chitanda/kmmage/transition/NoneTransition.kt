package cn.chitanda.kmmage.transition

import cn.chitanda.kmmage.request.ErrorResult
import cn.chitanda.kmmage.request.ImageResult
import cn.chitanda.kmmage.request.SuccessResult
import cn.chitanda.kmmage.target.Target

internal class NoneTransition(
    private val target: Target,
    private val result: ImageResult
) : Transition {

    override fun transition() {
        when (result) {
            is SuccessResult -> target.onSuccess(result.bitmap)
            is ErrorResult -> target.onError(result.bitmap)
        }
    }

    class Factory : Transition.Factory {

        override fun create(target: Target, result: ImageResult): Transition {
            return NoneTransition(target, result)
        }

        override fun equals(other: Any?) = other is Factory

        override fun hashCode() = javaClass.hashCode()
    }
}