package com.hbd.book_be.exception

import com.hbd.book_be.util.SentryHelper
import io.swagger.v3.oas.annotations.Hidden
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.LocalDateTime

@RestControllerAdvice
@Hidden
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        SentryHelper.addBreadcrumb("GlobalExceptionHandler initialized", "application", "info")
    }

    data class ErrorResponse(
        val errorCode: String,
        val message: String,
        val timestamp: LocalDateTime = LocalDateTime.now()
    )

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ResponseEntity<ErrorResponse> {
        log.error("Validation failed. Code=${ex.errorCode}, message=${ex.message}", ex)

        // Sentry에 Validation 에러 로깅
        SentryHelper.captureException(
            ex,
            "Validation Exception",
            mapOf(
                "error_code" to ex.errorCode,
                "http_status" to ex.status.toString(),
                "exception_type" to "ValidationException"
            )
        )

        return ResponseEntity.status(ex.status).body(
            ErrorResponse(ex.errorCode, ex.message)
        )
    }

    @ExceptionHandler(KakaoBookInfoNotFoundException::class)
    fun handleKakaoBookInfoNotFoundException(ex: KakaoBookInfoNotFoundException): ResponseEntity<ErrorResponse> {
        log.error(
            "Kakao book information not found. Code=${ErrorCodes.KAKAO_BOOK_INFO_NOT_FOUND}, message=${ex.message}",
            ex
        )

        // Sentry에 Kakao API 에러 로깅
        SentryHelper.captureException(
            ex,
            "Kakao Book Info Not Found",
            mapOf(
                "error_code" to ErrorCodes.KAKAO_BOOK_INFO_NOT_FOUND,
                "http_status" to ex.status.toString(),
                "exception_type" to "KakaoBookInfoNotFoundException",
                "service" to "kakao_api"
            )
        )

        return ResponseEntity.status(ex.status).body(
            ErrorResponse(ErrorCodes.KAKAO_BOOK_INFO_NOT_FOUND, ex.message)
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unhandled exception occurred. Code=${ErrorCodes.INTERNAL_SERVER_ERROR}, message=${ex.message}", ex)

        // 현재 요청 정보 가져오기
        val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        val request = requestAttributes?.request

        // Sentry에 요청 컨텍스트와 함께 에러 로깅
        request?.let { SentryHelper.setRequestContext(it) }

        val sentryTags = mutableMapOf<String, String>(
            "error_code" to ErrorCodes.INTERNAL_SERVER_ERROR,
            "http_status" to HttpStatus.INTERNAL_SERVER_ERROR.toString(),
            "exception_type" to ex.javaClass.simpleName
        )

        // 요청 정보 추가
        request?.let {
            sentryTags["request_method"] = it.method
            sentryTags["request_uri"] = it.requestURI
        }

        SentryHelper.captureException(
            ex,
            "Unhandled Exception",
            sentryTags
        )

        // 더 구체적인 에러 정보
        val detailedMessage = StringBuilder()
        detailedMessage.append("Error: ${ex.message ?: "Unknown error"}")

        // 원인 예외 정보
        var cause = ex.cause
        var level = 1
        while (cause != null && level <= 3) {
            detailedMessage.append("\n  Cause $level: ${cause.javaClass.simpleName} - ${cause.message}")
            cause = cause.cause
            level++
        }

        // 스택 트레이스 정보
        val projectStackTrace = ex.stackTrace
            .filter { it.className.contains("com.hbd.book_be") }
            .take(3)

        if (projectStackTrace.isNotEmpty()) {
            detailedMessage.append("\n  Stack trace:")
            projectStackTrace.forEach {
                detailedMessage.append("\n    at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})")
            }
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(ErrorCodes.INTERNAL_SERVER_ERROR, detailedMessage.toString())
        )
    }
}