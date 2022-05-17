package cn.chitanda.kmmage.request

import kotlinx.coroutines.Job

/**
 * @author: Chen
 * @createTime: 2022/5/16 18:21
 * @description:
 **/
internal class DesktopRequestDelegate(job: Job, lifecycle: Any) : RequestDelegate(job, lifecycle) {

    override fun start() {
    }

    override fun complete() {
    }

    override fun dispose() {
        job.cancel()
    }

}

internal actual fun RequestDelegate(
    lifecycle: Any,
    job: Job
): RequestDelegate = DesktopRequestDelegate(job, lifecycle)