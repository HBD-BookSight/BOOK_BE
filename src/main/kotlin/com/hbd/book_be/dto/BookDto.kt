package com.hbd.book_be.dto

import com.hbd.book_be.domain.Book
import java.time.LocalDateTime

data class BookDto(
    val isbn: String,
    val title: String,
    val summary: String,
    val publishedDate: LocalDateTime,
    val titleImage: String?,
    val authorList: List<AuthorDto.Simple>,
    val translator: String?,
    val price: Int?,
    val publisher: PublisherDto.Simple,
) {

    companion object {
        fun fromEntity(book: Book): BookDto {
            val authorList = book.bookAuthorList.map {
                AuthorDto.Simple.fromEntity(it.author)
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
                publisher = PublisherDto.Simple.fromEntity(book.publisher)
            )
        }
    }

    data class Simple(
        val isbn: String,
        val title: String,
        val authorList: List<AuthorDto.Simple>,
        val publisher: PublisherDto.Simple
    ) {
        companion object {
            fun fromEntity(book: Book): Simple {
                val authorList = book.bookAuthorList.map {
                    AuthorDto.Simple.fromEntity(it.author)
                }

                return Simple(
                    isbn = book.isbn,
                    title = book.title,
                    authorList = authorList,
                    publisher = PublisherDto.Simple.fromEntity(book.publisher)
                )
            }
        }
    }

    data class Detail(
        val isbn: String,
        val title: String,
        val detailUrl: String?,
        val summary: String,
        val publishedDate: LocalDateTime,
        val titleImage: String?,
        val authorList: List<AuthorDto.Simple>,
        val translator: String?,
        val price: Int?,
        val publisher: PublisherDto.Simple,
        val contentsDtoList: List<ContentsDto>,
        val eventDtoList: List<EventDto>
    ) {
        companion object {
            fun fromEntity(
                book: Book
            ): Detail {
                val contentsDtoList = book.bookContentsList.map {
                    ContentsDto.fromEntity(it.contents)
                }

                val eventsDtoList = book.bookEventList.map {
                    EventDto.fromEntity(it.event)
                }

                val authorList = book.bookAuthorList.map {
                    AuthorDto.Simple.fromEntity(it.author)
                }

                return Detail(
                    isbn = book.isbn,
                    title = book.title,
                    detailUrl = book.detailUrl,
                    summary = book.summary,
                    publishedDate = book.publishedDate,
                    titleImage = book.titleImage,
                    authorList = authorList,
                    translator = book.translator,
                    price = book.price,
                    publisher = PublisherDto.Simple.fromEntity(book.publisher),
                    contentsDtoList = contentsDtoList,
                    eventDtoList = eventsDtoList
                )
            }
        }
    }

}
