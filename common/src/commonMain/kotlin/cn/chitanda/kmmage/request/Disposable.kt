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