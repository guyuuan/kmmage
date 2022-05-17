package cn.chitanda.kmmage

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import okio.BufferedSource

/**
 * @author: Chen
 * @createTime: 2022/5/13 09:57
 * @description:
 **/
internal actual fun buildComponentRegistry(componentRegistry: ComponentRegistry): ComponentRegistry {
    return componentRegistry.newBuilder().build()
}

actual fun getPlatformName(): String {
    return "Android"
}

internal actual val KtorEngine: HttpClientEngineFactory<HttpClientEngineConfig> = CIO

internal actual fun BufferedSource.toImageBitmap(): ImageBitmap {

    return try {
        BitmapFactory.decodeStream(buffer.inputStream()).asImageBitmap()
    } catch (e: Exception) {
        throw  e
    }
}