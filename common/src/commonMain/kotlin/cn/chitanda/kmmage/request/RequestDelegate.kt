package cn.chitanda.kmmage.request

import kotlinx.coroutines.Job

/**
 * @author: Chen
 * @createTime: 2022/5/16 18:21
 * @description:
 **/
internal abstract class RequestDelegate(
    internal val job: Job,
    internal val lifecycle: Any
) {

    abstract fun start()

    abstract fun complete()

    abstract fun dispose()
}

internal expect fun RequestDelegate(lifecycle: Any,job: Job) :RequestDelegate