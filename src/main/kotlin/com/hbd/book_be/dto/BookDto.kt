package com.hbd.book_be.dto

import com.hbd.book_be.domain.Book
import java.time.LocalDateTime

data class BookDto(
    val isbn: String,
    val title: String,
    val summary: String,
    val publishedDate: LocalDateTime,
    val titleImage: String?,
    val authorList: List<AuthorDto>,
    val translator: String?,
    val price: Int?,
    val publisher: PublisherDto,
) {
    companion object {
        fun fromEntity(book: Book): BookDto {
            val authorList = book.bookAuthorList.map {
                AuthorDto.fromEntity(it.author)
            }

            return BookDto(
                isbn = book.isbn,
                title = book.title,
                summary = book.summary,
                publishedDate = book.publishedDate,
                titleImage = book.titleImage,
                authorList = authorList,
                translator = book.translator,
                price = book.price,
                publisher = PublisherDto.fromEntity(book.publisher)
            )
        }
    }
}
