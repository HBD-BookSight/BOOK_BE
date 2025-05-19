package com.hbd.book_be.event.model

import java.time.LocalDateTime

data class BookSearchEvent(
    val requestId: String,
    val totalCount: Long?,
    val keyword: String?,
    val status: String,
    val durationMs: Long,
    val errorMessage: String? = null,
    val searchDateTime: LocalDateTime = LocalDateTime.now()
)
