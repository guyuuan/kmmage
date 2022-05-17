package cn.chitanda.kmmage.util

import cn.chitanda.kmmage.RealImageLoader
import java.lang.ref.WeakReference

/**
 * @author: Chen
 * @createTime: 2022/5/13 10:55
 * @description:
 **/
internal abstract class SystemCallback(
    imageLoader: RealImageLoader,
    isNetworkObserverEnabled: Boolean
) {
    internal val imageLoader = WeakReference(imageLoader)
    abstract val isOnline: Boolean
    abstract val isShutDown: Boolean
}

internal expect fun SystemCallbacks(
    imageLoader: RealImageLoader,
    isNetworkObserverEnabled: Boolean
): SystemCallback