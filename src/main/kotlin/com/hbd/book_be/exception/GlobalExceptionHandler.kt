package com.hbd.book_be.exception

import io.swagger.v3.oas.annotations.Hidden
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
@Hidden
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(this::class.java)

    data class ErrorResponse(
        val errorCode: String,
        val message: String,
        val timestamp: LocalDateTime = LocalDateTime.now()
    )

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ResponseEntity<ErrorResponse> {
        log.error("Validation failed. Code=${ex.errorCode}, message=${ex.message}", ex)
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
        return ResponseEntity.status(ex.status).body(
            ErrorResponse(ErrorCodes.KAKAO_BOOK_INFO_NOT_FOUND, ex.message)
        )
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<ErrorResponse> {
        log.error("Resource not found. message=${ex.message}", ex)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse("NOT_FOUND", ex.message ?: "Resource not found")
        )
    }

    @ExceptionHandler(IllegalAccessException::class)
    fun handleIllegalAccessException(ex: IllegalAccessException): ResponseEntity<ErrorResponse> {
        log.error("Access denied. message=${ex.message}", ex)
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ErrorResponse("ACCESS_DENIED", ex.message ?: "Access denied")
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        log.error("Invalid argument. message=${ex.message}", ex)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse("INVALID_ARGUMENT", ex.message ?: "Invalid argument")
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unhandled exception occurred. Code=${ErrorCodes.INTERNAL_SERVER_ERROR}, message=${ex.message}", ex)
        
        // 더 구체적인 에러 정보 수집
        val detailedMessage = buildString {
            append("Error: ${ex.message ?: "Unknown error"}")
            
            // 원인 예외 정보 추가
            var cause = ex.cause
            var level = 1
            while (cause != null && level <= 3) {
                append("\n  Cause $level: ${cause::class.simpleName} - ${cause.message}")
                cause = cause.cause
                level++
            }
            
            // 스택 트레이스의 첫 번째 관련 라인 추가
            val relevantStackTrace = ex.stackTrace
                .filter { it.className.contains("com.hbd.book_be") }
                .take(3)
                .joinToString("\n") { "    at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})" }
            
            if (relevantStackTrace.isNotEmpty()) {
                append("\n  Stack trace:\n$relevantStackTrace")
            }
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(ErrorCodes.INTERNAL_SERVER_ERROR, detailedMessage)
        )
    }
}