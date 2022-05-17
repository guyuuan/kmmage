package cn.chitanda.kmmage.util

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import cn.chitanda.kmmage.request.ImageRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal actual suspend fun ImageRequest.awaitStart() {
    check(lifecycle is Lifecycle) {
        "ImageRequest lifecycle must is androidx.lifecycle "
    }
    lifecycle.awaitStart()
}

internal suspend fun Lifecycle.awaitStart() {
    // Fast path: we're already started.
    if (currentState.isAtLeast(Lifecycle.State.STARTED)) return

    // Slow path: observe the lifecycle until we're started.
    var observer: LifecycleObserver? = null
    try {
        suspendCancellableCoroutine<Unit> { continuation ->
            observer = object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    continuation.resume(Unit)
                }
            }
            addObserver(observer!!)
        }
    } finally {
        // 'observer' will always be null if this method is marked as 'inline'.
        observer?.let(::removeObserver)
    }
}