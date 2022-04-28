package cn.chitanda.kmmage.util

import io.ktor.client.request.HttpRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.takeFrom

/**
 * @author: Chen
 * @createTime: 2022/4/28 11:10
 * @description:
 **/
fun HttpRequest.newBuilder(): HttpRequestBuilder {
    return HttpRequestBuilder().takeFrom(this)
}
fun HttpRequestData.newBuilder(): HttpRequestBuilder {
    return HttpRequestBuilder().takeFrom(this)
}