package com.hbd.book_be.event.model

import java.time.LocalDateTime

data class BookViewEvent(
    val requestId: String,
    val isbn: String,
    val title: String,
    val userId: Long?,
    val status: String,
    val errorMessage: String?,
    val durationMs: Long?,
    val sourceKeyword: String?,
    val sourcePath: String?,
    val viewDateTime: LocalDateTime = LocalDateTime.now(),
)