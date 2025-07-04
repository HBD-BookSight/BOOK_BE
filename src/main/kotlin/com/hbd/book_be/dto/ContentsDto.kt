package com.hbd.book_be.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.hbd.book_be.domain.Contents
import com.hbd.book_be.domain.common.UrlInfo

data class ContentsDto(
    val id: Long,
    val title: String?,
    val urls: List<UrlInfo>,
    val image: String?,

    @JsonProperty("creator")
    val creatorDto: UserDto,
) {
    companion object {
        fun fromEntity(contents: Contents): ContentsDto {
            if (contents.id == null) {
                throw IllegalArgumentException("Contents id can't be null")
            }

            return ContentsDto(
                id = contents.id!!,
                title = contents.title,
                urls = contents.urls,
                image = contents.image,
                creatorDto = UserDto.fromEntity(contents.creator)
            )
        }
    }

    data class Detail(
        val id: Long,
        val title: String?,
        val urls: List<UrlInfo>,
        val image: String?,

        @JsonProperty(value = "books")
        val booksDtoList: List<BookDto>,
        @JsonProperty(value = "tags")
        val tagDtoList: List<TagDto>,

        @JsonProperty("creator")
        val creatorDto: UserDto
    ) {
        companion object {
            fun fromEntity(contents: Contents): Detail {
                if (contents.id == null) {
                    throw IllegalArgumentException("Contents id can't be null")
                }

                val booksDtoList = contents.bookContentsList.map {
                    BookDto.fromEntity(it.book)
                }

                val tagDtoList = contents.tagContentsList.map {
                    TagDto.fromEntity(it.tag)
                }

                return Detail(
                    id = contents.id!!,
                    title = contents.title,
                    urls = contents.urls,
                    image = contents.image,
                    creatorDto = UserDto.fromEntity(contents.creator),
                    booksDtoList = booksDtoList,
                    tagDtoList = tagDtoList
                )
            }
        }
    }
}