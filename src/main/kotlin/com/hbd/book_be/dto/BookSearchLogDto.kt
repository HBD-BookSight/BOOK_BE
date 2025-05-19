package com.hbd.book_be.dto

import com.hbd.book_be.domain.BookSearchLog
import java.time.LocalDateTime

data class BookSearchLogDto(
    val requestId: String,
    val keyword: String?,
    val totalCount: Long?,
    val status: String,
    val durationMs: Long,
    val errorMessage: String?,
    val searchDateTime: LocalDateTime,
){
    companion object {
        fun fromEntity(entity: BookSearchLog): BookSearchLogDto {
            return BookSearchLogDto(
                requestId = entity.requestId,
                keyword = entity.keyword,
                totalCount = entity.totalCount,
                status = entity.status,
                durationMs = entity.durationMs,
                errorMessage = entity.errorMessage,
                searchDateTime = entity.searchDateTime,
            )
        }
    }
}
