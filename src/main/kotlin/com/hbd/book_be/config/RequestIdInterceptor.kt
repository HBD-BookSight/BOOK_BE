package com.hbd.book_be.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.*


@Component
class RequestIdInterceptor : HandlerInterceptor {
    companion object {
        const val REQUEST_ID_KEY = "requestId"
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val requestId = UUID.randomUUID().toString()
        MDC.put(REQUEST_ID_KEY, requestId)
        response.addHeader("X-Request-ID", requestId)
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        MDC.remove(REQUEST_ID_KEY)
    }
} 