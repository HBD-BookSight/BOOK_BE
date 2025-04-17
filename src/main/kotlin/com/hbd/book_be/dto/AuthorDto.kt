package com.hbd.book_be.dto

import com.hbd.book_be.domain.Author

data class AuthorDto(
    val id: Long,
    val name: String,
    val description: String? = null,
    val profile: String? = null,
) {
    companion object {
        fun fromEntity(author: Author): AuthorDto {
            if (author.id == null) {
                throw IllegalArgumentException("Author id can not be null.")
            }

            return AuthorDto(
                id = author.id!!,
                name = author.name,
                profile = author.profile,
            )
        }
    }
}