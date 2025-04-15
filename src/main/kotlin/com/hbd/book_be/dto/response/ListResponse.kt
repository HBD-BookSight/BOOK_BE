package com.hbd.book_be.dto.response

data class ListResponse<T>(
    val items: List<T>,
    val length: Int,
)