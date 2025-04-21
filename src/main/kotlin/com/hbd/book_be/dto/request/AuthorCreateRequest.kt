package com.hbd.book_be.dto.request

import io.swagger.v3.oas.annotations.media.Schema

data class AuthorCreateRequest(
    val name: String,
    val description: String? = null,
    val profile: String? = null,

    @field:Schema(defaultValue = "[]")
    val bookIsdnList: List<String> = emptyList()
)