package com.hbd.book_be.dto

import com.hbd.book_be.domain.Tag

data class TagDto(
    val id: Long,
    val name: String
) {
    companion object {
        fun fromEntity(tag: Tag): TagDto {
            if(tag.id == null){
                throw IllegalArgumentException("User id can't be null")
            }
            return TagDto(
                id = tag.id!!,
                name = tag.name
            )
        }
    }
}