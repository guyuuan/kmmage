package cn.chitanda.common.http

import cn.chitanda.common.KtorEngine
import cn.chitanda.common.toImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * @author: Chen
 * @createTime: 2022/3/10 15:49
 * @description:
 **/
object Api {
    private val client = HttpClient(KtorEngine)

    suspend fun getWebHTML(address: String): String =
        withContext(Dispatchers.IO) {
            client.get { url(address) }.body<String>().toString()
        }

    suspend fun getImageBitmap(address: String) = withContext(Dispatchers.IO) {
        val response = client.get {
            url(address)
        }
        val ba: ByteArray = response.body()
        ba.toImageBitmap()
    }
}
