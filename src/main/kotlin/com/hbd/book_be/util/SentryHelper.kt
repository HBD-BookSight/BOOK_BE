package com.hbd.book_be.util

import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.protocol.User
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Sentry 헬퍼 유틸리티 클래스
 * application.yml의 sentry.enabled 설정에 따라 조건부로 활성화됩니다.
 */
@Component
@ConditionalOnProperty(
    prefix = "sentry",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false
)
object SentryHelper {
    
    /**
     * 사용자 정의 에러 로깅
     */
    fun captureException(throwable: Throwable, message: String? = null, tags: Map<String, String> = emptyMap()) {
        // 태그 설정
        tags.forEach { (key, value) ->
            Sentry.setTag(key, value)
        }
        
        // 메시지가 있으면 extra로 추가
        message?.let {
            Sentry.setExtra("custom_message", it)
        }
        
        // 예외 전송
        Sentry.captureException(throwable)
    }
    
    /**
     * 사용자 정의 메시지 로깅
     */
    fun captureMessage(message: String, tags: Map<String, String> = emptyMap()) {
        // 태그 설정
        tags.forEach { (key, value) ->
            Sentry.setTag(key, value)
        }
        
        // 메시지 전송
        Sentry.captureMessage(message)
    }
    
    /**
     * Breadcrumb 추가
     */
    fun addBreadcrumb(message: String, category: String = "custom", level: String = "info") {
        val breadcrumb = Breadcrumb().apply {
            this.message = message
            this.category = category
            this.level = when (level.lowercase()) {
                "debug" -> io.sentry.SentryLevel.DEBUG
                "info" -> io.sentry.SentryLevel.INFO
                "warning" -> io.sentry.SentryLevel.WARNING
                "error" -> io.sentry.SentryLevel.ERROR
                "fatal" -> io.sentry.SentryLevel.FATAL
                else -> io.sentry.SentryLevel.INFO
            }
        }
        Sentry.addBreadcrumb(breadcrumb)
    }
    
    /**
     * 사용자 컨텍스트 설정
     */
    fun setUserContext(userId: String?, email: String? = null, username: String? = null) {
        val user = User().apply {
            id = userId
            this.email = email
            this.username = username
        }
        Sentry.setUser(user)
    }
    
    /**
     * 요청 컨텍스트 설정
     */
    fun setRequestContext(request: HttpServletRequest) {
        Sentry.setExtra("request_url", request.requestURL.toString())
        Sentry.setExtra("request_method", request.method)
        Sentry.setExtra("request_params", request.parameterMap.toString())
        Sentry.setTag("request_uri", request.requestURI)
    }
}
