package com.hbd.book_be.dto

import com.hbd.book_be.domain.Book
import java.time.LocalDateTime

data class BookDetailedDto(
    val isbn: String,
    val title: String,
    val detailUrl: String?,
    val summary: String,
    val publishedDate: LocalDateTime,
    val titleImage: String?,
    val authorList: List<AuthorDto>,
    val translator: String?,
    val price: Int?,
    val publisher: PublisherDto,
    val contentsDtoList: List<ContentsDto>,
    val eventDtoList: List<EventDto>
) {
    companion object {
        fun fromEntity(
            book: Book
        ): BookDetailedDto {
            val contentsDtoList = book.bookContentsList.map {
                ContentsDto.fromEntity(it.contents)
            }

            val eventsDtoList = book.bookEventList.map {
                EventDto.fromEntity(it.event)
            }

            val authorList = book.bookAuthorList.map {
                AuthorDto.fromEntity(it.author)
            }

            return BookDetailedDto(
                isbn = book.isbn,
                title = book.title,
                detailUrl = book.detailUrl,
                summary = book.summary,
                publishedDate = book.publishedDate,
                titleImage = book.titleImage,
                authorList = authorList,
                translator = book.translator,
                price = book.price,
                publisher = PublisherDto.fromEntity(book.publisher),
                contentsDtoList = contentsDtoList,
                eventDtoList = eventsDtoList
            )
        }
    }
}
