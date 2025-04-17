package com.hbd.book_be.dto.request

data class AuthorCreateRequest(
    val name: String,
    val description: String? = null,
    val profile: String? = null,
    val bookIsdnList: List<String> = emptyList()
)