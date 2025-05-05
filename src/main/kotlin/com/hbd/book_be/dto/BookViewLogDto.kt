package com.hbd.book_be.dto

import com.hbd.book_be.domain.BookViewLog
import java.time.LocalDateTime

data class BookViewLogDto(
    val requestId: String,
    val isbn: String,
    val title: String,
    val userId: Long?,
    val status: String,
    val durationMs: Long?,
    val errorMessage: String?,
    val sourceKeyword: String?,
    val sourcePath: String?,
    val viewDateTime: LocalDateTime?,
){

    companion object {
        fun fromEntity(entity: BookViewLog): BookViewLogDto {
            return BookViewLogDto(
                requestId = entity.requestId,
                isbn=entity.isbn,
                title=entity.title,
                userId = entity.userId,
                status=entity.status,
                durationMs=entity.durationMs,
                errorMessage=entity.errorMessage,
                sourceKeyword=entity.sourceKeyword,
                sourcePath=entity.sourcePath,
                viewDateTime=entity.viewDateTime,
            )
        }
    }
}
