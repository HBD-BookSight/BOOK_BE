package com.hbd.book_be.dto

import com.hbd.book_be.domain.Book
import java.time.LocalDateTime

data class BookDto(
    val isbn: String,
    val title: String,
    val summary: String,
    val publishedDate: LocalDateTime,
    val titleImage: String?,
    val author: AuthorDto,
    val translator: String?,
    val price: Int?,
    val publisher: PublisherDto,
) {
    companion object {
        fun fromEntity(book: Book): BookDto {
            return BookDto(
                isbn = book.isbn,
                title = book.title,
                summary = book.summary,
                publishedDate = book.publishedDate,
                titleImage = book.titleImage,
                author = AuthorDto.fromEntity(book.author),
                translator = book.translator,
                price = book.price,
                publisher = PublisherDto.fromEntity(book.publisher)
            )
        }
    }
}
