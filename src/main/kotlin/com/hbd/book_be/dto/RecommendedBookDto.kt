package com.hbd.book_be.dto

import com.hbd.book_be.domain.RecommendedBook
import java.time.LocalDate
import java.time.LocalDateTime

data class RecommendedBookDto(
    val isbn: String,
    val title: String,
    val summary: String,
    val publishedDate: LocalDateTime,
    val titleImage: String?,
    val authorList: List<AuthorDto>,
    val translator: String?,
    val price: Int?,
    val publisher: PublisherDto,
    val recommendedDate: LocalDate
) {
    companion object {
        fun fromEntity(recommendedBook: RecommendedBook): RecommendedBookDto {
            val book = recommendedBook.book
            val authorList = book.bookAuthorList.map {
                AuthorDto.fromEntity(it.author)
            }

            return RecommendedBookDto(
                isbn = book.isbn,
                title = book.title,
                summary = book.summary,
                publishedDate = book.publishedDate,
                titleImage = book.titleImage,
                authorList = authorList,
                translator = book.translator,
                price = book.price,
                publisher = PublisherDto.fromEntity(book.publisher),
                recommendedDate = recommendedBook.recommendedDate
            )
        }
    }
}