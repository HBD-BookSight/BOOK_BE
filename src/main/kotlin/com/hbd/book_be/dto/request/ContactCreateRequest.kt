package com.hbd.book_be.dto.request


data class ContactCreateRequest(
    val name: String?,
    val email: String,
    val message: String
)
