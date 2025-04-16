package com.hbd.book_be.exception

data class NotFoundException(
    override val message: String
) : RuntimeException(message)