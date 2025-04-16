package com.hbd.book_be.exception

import org.springframework.http.HttpStatus

data class ValidationException(
    override val message: String,
    val errorCode: String,
    val status: HttpStatus = HttpStatus.BAD_REQUEST
) : RuntimeException(message)