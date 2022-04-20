package cn.chitanda.common

import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.client.engine.*

expect fun getPlatformName(): String
internal expect val KtorEngine: HttpClientEngineFactory<HttpClientEngineConfig>
internal expect fun ByteArray.toImageBitmap(): ImageBitmap