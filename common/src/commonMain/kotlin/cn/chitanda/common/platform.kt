package cn.chitanda.common

import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.client.engine.*
import okio.BufferedSource
import java.io.InputStream

expect fun getPlatformName(): String
internal expect val KtorEngine: HttpClientEngineFactory<HttpClientEngineConfig>
internal expect fun BufferedSource.toImageBitmap(): ImageBitmap