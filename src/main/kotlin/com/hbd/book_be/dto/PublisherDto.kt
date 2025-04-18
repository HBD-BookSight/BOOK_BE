package com.hbd.book_be.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.hbd.book_be.domain.Publisher
import com.hbd.book_be.domain.common.UrlInfo

data class PublisherDto(
    val id: Long,
    val name: String,
    val engName: String?,
    val logo: String?,
    val isOfficial: Boolean,
    val description: String?,
    val urls: List<UrlInfo>,
) {
    companion object {
        fun fromEntity(publisher: Publisher): PublisherDto {
            if (publisher.id == null) {
                throw IllegalArgumentException("Publisher id can not be null.")
            }

            return PublisherDto(
                id = publisher.id!!,
                name = publisher.name,
                engName = publisher.engName,
                logo = publisher.logo,
                isOfficial = publisher.isOfficial,
                description = publisher.description,
                urls = publisher.urls,
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
        val engName: String?,
        val logo: String?,
        val isOfficial: Boolean,
        val description: String?,
        val urls: List<UrlInfo>,

        @JsonProperty(value = "books")
        val bookDtoList: List<BookDto>,

        @JsonProperty(value = "tags")
        val tagDtoList: List<TagDto>,
    ) {
        companion object {
            fun fromEntity(publisher: Publisher): Detail {
                if (publisher.id == null) {
                    throw IllegalArgumentException("Publisher id can not be null.")
                }

                val bookDtoList = publisher.bookList.map {
                    BookDto.fromEntity(it)
                }

                val tagDtoList = publisher.tagPublisherList.map {
                    TagDto.fromEntity(it.tag)
                }

                return Detail(
                    id = publisher.id!!,
                    name = publisher.name,
                    engName = publisher.engName,
                    logo = publisher.logo,
                    isOfficial = publisher.isOfficial,
                    description = publisher.description,
                    urls = publisher.urls,
                    bookDtoList = bookDtoList,
                    tagDtoList = tagDtoList
                )
            }
        }
    }
}