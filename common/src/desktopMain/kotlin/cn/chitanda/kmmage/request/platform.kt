package cn.chitanda.kmmage.request

import cn.chitanda.kmmage.size.Size
import cn.chitanda.kmmage.size.SizeResolver

private val DesktopSizeResolver = SizeResolver { Size(windowBounds.first, windowBounds.second) }

internal actual val DEFAULT_REQUEST_OPTION: DefaultRequestOptions = DefaultRequestOptions()
internal actual val DefaultSizeResolver: SizeResolver = DesktopSizeResolver

private var windowBounds = 0 to 0
fun updateWindowBounds(bounds: Pair<Int, Int>) {
    windowBounds = bounds
}