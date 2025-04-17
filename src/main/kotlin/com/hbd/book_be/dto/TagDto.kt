package com.hbd.book_be.dto

import com.hbd.book_be.domain.Tag

data class TagDto(
    val name: String
) {
    companion object {
        fun fromEntity(tag: Tag): TagDto {
            return TagDto(name = tag.name)
        }
    }
}