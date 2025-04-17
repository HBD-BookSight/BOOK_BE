package com.hbd.book_be.exception

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Hidden
class GlobalExceptionHandler {

    data class ErrorResponse(
        val errorCode: String,
        val message: String
    )

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(ex.status).body(
            ErrorResponse(ex.errorCode, ex.message)
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(ErrorCodes.INTERNAL_SERVER_ERROR, ex.message ?: "")
        )
    }
}