package com.hbd.book_be.dto.response

data class PageResponse<T>(
    val items: List<T>,
    val totalCount: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)