package cn.chitanda.kmmage.request

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Job

/**
 * @author: Chen
 * @createTime: 2022/5/16 18:21
 * @description:
 **/
internal class AndroidRequestDelegate(job: Job, lifecycle: Lifecycle) :
    RequestDelegate(job, lifecycle), DefaultLifecycleObserver {

    private val _lifecycle: Lifecycle get() = lifecycle as Lifecycle
    override fun start() {
        _lifecycle.addObserver(this)
    }

    override fun complete() {
        _lifecycle.removeObserver(this)
    }

    override fun dispose() {
        job.cancel()
    }

    override fun onDestroy(owner: LifecycleOwner) = dispose()
}

internal actual fun RequestDelegate(
    lifecycle: Any,
    job: Job
): RequestDelegate {
    check(lifecycle is Lifecycle) {
        "RequestDelegate lifecycle must is androidx.lifecycle "
    }
    return AndroidRequestDelegate(job, lifecycle)
}