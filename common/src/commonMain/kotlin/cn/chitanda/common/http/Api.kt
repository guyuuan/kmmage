package cn.chitanda.common.http

import cn.chitanda.common.KtorEngine
import cn.chitanda.kmmage.ImageLoader
import cn.chitanda.kmmage.request.ImageRequest
import cn.chitanda.kmmage.request.Options
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author: Chen
 * @createTime: 2022/3/10 15:49
 * @description:
 **/
object Api {
    private val client = HttpClient(KtorEngine)
    private val imageLoader = ImageLoader.Builder().build()
    suspend fun getWebHTML(address: String): String =
        withContext(Dispatchers.IO) {
            client.get { url(address) }.body<String>().toString()
        }

    suspend fun getImageBitmap(address: String) = withContext(Dispatchers.IO) {
        imageLoader.request(
            ImageRequest.Builder().data(address).build(),
            options = Options()
        ).bitmap
//        val response = client.get {
//            url(address)
//        }
//        val ba: ByteArray = response.body()
//        ba.toImageBitmap()
    }
}
