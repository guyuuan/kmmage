package cn.chitanda.kmmage.util

import cn.chitanda.kmmage.size.Size
import cn.chitanda.kmmage.size.pxOrElse

/**
 * @author: Chen
 * @createTime: 2022/5/13 15:03
 * @description:
 **/
internal sealed class HardwareBitmapService {

    /** Return 'true' if we are currently able to create [Bitmap.Config.HARDWARE]. */
    abstract fun allowHardwareMainThread(size: Size): Boolean

    /** Perform any hardware bitmap allocation checks that cannot be done on the main thread. */
    abstract fun allowHardwareWorkerThread(): Boolean

}

internal expect fun HardwareBitmapService(): HardwareBitmapService

/** Returns a fixed value for [allowHardwareMainThread] and [allowHardwareWorkerThread]. */
internal class ImmutableHardwareBitmapService(private val allowHardware: Boolean) :
    HardwareBitmapService() {
    override fun allowHardwareMainThread(size: Size) = allowHardware
    override fun allowHardwareWorkerThread() = allowHardware
}

/** Guards against running out of file descriptors. */
internal class LimitedFileDescriptorHardwareBitmapService(
    private val allowHardwareWorkerThread: () -> Boolean
) : HardwareBitmapService() {

    override fun allowHardwareMainThread(size: Size): Boolean {
        return size.width.pxOrElse { Int.MAX_VALUE } > MIN_SIZE_DIMENSION &&
                size.height.pxOrElse { Int.MAX_VALUE } > MIN_SIZE_DIMENSION
    }

    override fun allowHardwareWorkerThread(): Boolean {
        return allowHardwareWorkerThread.invoke()
    }

    companion object {
        private const val MIN_SIZE_DIMENSION = 100
    }
}