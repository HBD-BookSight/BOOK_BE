package com.hbd.book_be.exception

import org.springframework.http.HttpStatus

class KakaoBookInfoNotFoundException(
    override val message: String,
    val status: HttpStatus = HttpStatus.BAD_REQUEST
) : RuntimeException(message)