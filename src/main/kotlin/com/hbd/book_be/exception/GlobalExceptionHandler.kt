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

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unhandled exception occurred. Code=${ErrorCodes.INTERNAL_SERVER_ERROR}, message=${ex.message}", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(ErrorCodes.INTERNAL_SERVER_ERROR, ex.message ?: "")
        )
    }
}