package cn.chitanda.kmmage

import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import okio.BufferedSource

/**
 * @author: Chen
 * @createTime: 2022/5/13 09:57
 * @description:
 **/

expect fun getPlatformName(): String

internal expect  fun buildComponentRegistry(componentRegistry: ComponentRegistry):ComponentRegistry
internal expect val KtorEngine: HttpClientEngineFactory<HttpClientEngineConfig>
internal expect fun BufferedSource.toImageBitmap(): ImageBitmap