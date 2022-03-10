package cn.chitanda.common.http

import cn.chitanda.common.KtorEngine
import cn.chitanda.common.toImageBitmap
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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