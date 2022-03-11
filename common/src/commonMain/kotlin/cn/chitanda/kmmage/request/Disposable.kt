package cn.chitanda.kmmage.request

import kotlinx.coroutines.Deferred

/**
 * Represents the work of an [ImageRequest] that has been executed by an [ImageLoader].
 */
interface Disposable {

    /**
     * The most recent image request job.
     * This field is **not immutable** and can change if the request is replayed.
     */
    val job: Deferred<ImageResult>

    /**
     * Returns 'true' if this disposable's work is complete or cancelling.
     */
    val isDisposed: Boolean

    /**
     * Cancels this disposable's work and releases any held resources.
     */
    fun dispose()
}