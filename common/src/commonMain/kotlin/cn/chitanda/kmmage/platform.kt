package cn.chitanda.kmmage

import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okio.BufferedSource
import okio.Path

/**
 * @author: Chen
 * @createTime: 2022/5/13 09:57
 * @description:
 **/

expect fun getPlatformName(): String

internal expect  fun ComponentRegistry.addPlatformComponentRegistry():ComponentRegistry
internal expect val KtorEngine: HttpClientEngineFactory<HttpClientEngineConfig>
internal expect suspend fun BufferedSource.toImageBitmap(): ImageBitmap
internal expect val PlatformMainDispatcher:CoroutineDispatcher