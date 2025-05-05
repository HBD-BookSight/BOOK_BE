package com.hbd.book_be.util

import com.hbd.book_be.config.RequestIdInterceptor
import org.slf4j.MDC

object RequestContext {
    fun getRequestId(): String {
        return MDC.get(RequestIdInterceptor.REQUEST_ID_KEY) ?: "unknown"
    }
} 