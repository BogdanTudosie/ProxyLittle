package com.bogdantudosie.proxylittle.services

import android.util.Log
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpObject
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import org.littleshoot.proxy.HttpFilters
import org.littleshoot.proxy.HttpFiltersAdapter
import org.littleshoot.proxy.HttpFiltersSource
import org.littleshoot.proxy.HttpFiltersSourceAdapter
import org.littleshoot.proxy.impl.DefaultHttpProxyServer
import java.net.InetSocketAddress

class LittleProxyServer {
    private val TAG = "LittleProxyServer"
    private val port = 8080

    fun runService() {
        val httpFilterSource: HttpFiltersSource = getFiltersSource()

        DefaultHttpProxyServer.bootstrap()
            .withAddress(InetSocketAddress("127.0.0.1", port))
            .withAllowLocalOnly(false)
            .withFiltersSource(httpFilterSource)
            .withName("Background Little Proxy")
            .start()
    }

    private fun getFiltersSource(): HttpFiltersSource {
        return object: HttpFiltersSourceAdapter() {
            override fun filterRequest(originalRequest: HttpRequest): HttpFilters {
                return object: HttpFiltersAdapter(originalRequest) {
                    override fun clientToProxyRequest(httpObject: HttpObject): HttpResponse? {
                        if (httpObject is HttpRequest) {
                            Log.i(TAG, "Method URI: ${httpObject.method()} ${httpObject.uri()}")
                            if (httpObject.uri().endsWith("png") || httpObject.uri()
                                    .endsWith("jpeg")) {
                                return getBadGatewayResponse()
                            }
                        }
                        return null
                    }

                    private fun getBadGatewayResponse(): HttpResponse {
                        val body = """
                         <!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML 2.0//EN">
                         <html><head>
                         <title>Bad Gateway</title>
                         </head><body>
                         An error occurred
                         </body></html>
                         """.trimIndent()
                        val bytes = body.toByteArray(Charsets.UTF_8)
                        val content = Unpooled.copiedBuffer(bytes)
                        val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY, content)
                        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.size.toString())
                        response.headers().set("Content-Type", "text/html; charset=UTF-8")
                        // response.headers().set("Date", ProxyUtils.formatDate(Date()))
                        response.headers().set(HttpHeaders.Names.CONNECTION, "close")
                        return response
                    }
                }
            }
        }
    }
}