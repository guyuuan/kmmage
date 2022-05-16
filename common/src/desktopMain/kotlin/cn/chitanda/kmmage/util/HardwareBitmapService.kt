package cn.chitanda.kmmage.util

internal actual fun HardwareBitmapService(): HardwareBitmapService =
    ImmutableHardwareBitmapService(true)