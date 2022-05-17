package cn.chitanda.kmmage.request

import kotlinx.coroutines.Deferred

/**
 * @author: Chen
 * @createTime: 2022/5/7 17:37
 * @description:
 **/
interface Disposable {
    val job: Deferred<ImageResult>
    val isDisposed: Boolean
    fun dispose()
}

internal class OneShotDisposable(
    override val job: Deferred<ImageResult>
) : Disposable {

    override val isDisposed: Boolean
        get() = !job.isActive

    override fun dispose() {
        if (isDisposed) return
        job.cancel()
    }
}