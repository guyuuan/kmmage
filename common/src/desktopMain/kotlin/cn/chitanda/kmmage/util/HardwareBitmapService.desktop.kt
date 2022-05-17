package cn.chitanda.kmmage.util

internal actual fun HardwareBitmapServices(): HardwareBitmapService =
    ImmutableHardwareBitmapService(true)