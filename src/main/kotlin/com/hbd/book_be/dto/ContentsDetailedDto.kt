package com.hbd.book_be.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.hbd.book_be.domain.Contents
import com.hbd.book_be.domain.enums.ContentType

data class ContentsDetailedDto(
    val id: Long,
    val type: ContentType,
    val url: String,
    val image: String?,
    val booksDtoList: List<BookDto>,
    val tagDtoList: List<TagDto>,

    @JsonProperty("creator")
    val creatorDto: UserDto
) {
    companion object {
        fun fromEntity(contents: Contents): ContentsDetailedDto {

            if (contents.id == null) {
                throw IllegalArgumentException("Contents id can't be null")
            }

            val booksDtoList = contents.bookContentsList.map {
                BookDto.fromEntity(it.book)
            }

            val tagDtoList = contents.tagContentsList.map {
                TagDto.fromEntity(it.tag)
            }

            return ContentsDetailedDto(
                id = contents.id!!,
                type = contents.type,
                url = contents.url,
                image = contents.image,
                creatorDto = UserDto.fromEntity(contents.creator),
                booksDtoList = booksDtoList,
                tagDtoList = tagDtoList
            )
        }
    }
}