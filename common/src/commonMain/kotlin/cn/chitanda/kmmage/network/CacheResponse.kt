package cn.chitanda.kmmage.network

import cn.chitanda.kmmage.util.toContentTypeOrNull
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import okio.BufferedSink
import okio.BufferedSource
import kotlin.LazyThreadSafetyMode.NONE

/**
 * @author: Chen
 * @createTime: 2022/4/27 14:21
 * @description:
 **/
class CacheResponse {
    val cacheControl by lazy(NONE) { CacheControl.parse(responseHeaders) }
    val contentType by lazy(NONE) { responseHeaders[HttpHeaders.ContentType]?.toContentTypeOrNull() }
    val responseTime: Long
    val requestTime: Long
    val responseHeaders: Headers

    constructor(source: BufferedSource) {
        requestTime = source.readUtf8LineStrict().toLong()
        responseTime = source.readUtf8LineStrict().toLong()
        val responseHeadersLineCount = source.readUtf8LineStrict().toInt()
        responseHeaders = Headers.build {
            repeat(responseHeadersLineCount) {
                val str = source.readUtf8LineStrict()
                val entry = str.split(": ")
                this.append(entry[0], entry[1])
            }
        }
    }

    constructor(response: HttpResponse) {
        this.requestTime = response.requestTime.timestamp
        this.responseTime = response.responseTime.timestamp
        this.responseHeaders = response.headers
    }

    fun writeTo(sink: BufferedSink) {
        sink.writeDecimalLong(requestTime).writeByte('\n'.code)
        sink.writeDecimalLong(responseTime).writeByte('\n'.code)
        sink.writeDecimalLong(responseHeaders.names().size.toLong()).writeByte('\n'.code)
        for (entry in responseHeaders.entries()) {
            sink.writeUtf8(entry.key)
                .writeUtf8(": ")
                .writeUtf8(entry.value.joinToString(", "))
                .writeByte('\n'.code)
        }
    }
}