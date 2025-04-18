package com.hbd.book_be.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.hbd.book_be.domain.Publisher

data class PublisherDto(
    val id: Long,
    val name: String,
    val logo: String?,
    val isOfficial: Boolean,
    val description: String?,
    val link: String?,
) {
    companion object {
        fun fromEntity(publisher: Publisher): PublisherDto {
            if (publisher.id == null) {
                throw IllegalArgumentException("Publisher id can not be null.")
            }

            return PublisherDto(
                id = publisher.id!!,
                name = publisher.name,
                logo = publisher.logo,
                isOfficial = publisher.isOfficial,
                description = publisher.description,
                link = publisher.link,
            )
        }
    }

    data class Simple(
        val id: Long,
        val name: String
    ) {
        companion object {
            fun fromEntity(publisher: Publisher): Simple {
                if (publisher.id == null) {
                    throw IllegalArgumentException("Publisher id can not be null.")
                }

                return Simple(
                    id = publisher.id!!,
                    name = publisher.name
                )
            }
        }
    }

    data class Detail(
        val id: Long,
        val name: String,
        val logo: String?,
        val isOfficial: Boolean,
        val description: String?,
        val link: String?,

        @JsonProperty(value = "books")
        val bookDtoList: List<BookDto>
    ) {
        companion object {
            fun fromEntity(publisher: Publisher): Detail {
                if (publisher.id == null) {
                    throw IllegalArgumentException("Publisher id can not be null.")
                }

                val bookDtoList = publisher.bookList.map {
                    BookDto.fromEntity(it)
                }

                return Detail(
                    id = publisher.id!!,
                    name = publisher.name,
                    logo = publisher.logo,
                    isOfficial = publisher.isOfficial,
                    description = publisher.description,
                    link = publisher.link,
                    bookDtoList = bookDtoList
                )
            }
        }
    }
}