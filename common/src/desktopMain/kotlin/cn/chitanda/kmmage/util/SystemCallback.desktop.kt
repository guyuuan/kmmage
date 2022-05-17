package cn.chitanda.kmmage.util

import cn.chitanda.kmmage.RealImageLoader

internal actual fun SystemCallbacks(
    imageLoader: RealImageLoader,
    isNetworkObserverEnabled: Boolean
): SystemCallback {
    return object :SystemCallback(imageLoader, isNetworkObserverEnabled){
        override val isOnline: Boolean
            get() =true
        override val isShutDown: Boolean
            get() = false
    }
}