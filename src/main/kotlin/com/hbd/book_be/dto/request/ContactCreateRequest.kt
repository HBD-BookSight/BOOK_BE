package com.hbd.book_be.dto.request

data class ContactCreateRequest(
    val email: String,
    val message: String
)
