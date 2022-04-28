package cn.chitanda.kmmage.network

import io.ktor.client.statement.HttpResponse

/**
 * @author: Chen
 * @createTime: 2022/4/28 14:27
 * @description:
 **/
class HttpException(val response: HttpResponse) :
    RuntimeException("HTTP ${response.status}")