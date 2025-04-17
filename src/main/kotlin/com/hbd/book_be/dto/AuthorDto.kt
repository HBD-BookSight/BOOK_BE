package com.hbd.book_be.dto

import com.hbd.book_be.domain.Author

data class AuthorDto(
    val id: Long,
    val name: String,
    val description: String? = null,
    val profile: String? = null,
    val bookList: List<BookDto> = emptyList()
) {

    companion object {
        fun fromEntity(author: Author): AuthorDto {
            if (author.id == null) {
                throw IllegalArgumentException("Author id can not be null.")
            }

            val bookDtoList = author.bookAuthorList.map { BookDto.fromEntity(it.book) }

            return AuthorDto(
                id = author.id!!,
                name = author.name,
                profile = author.profile,
                bookList = bookDtoList
            )
        }
    }

    data class Simple(
        val id: Long,
        val name: String
    ) {
        companion object {
            fun fromEntity(author: Author): Simple {
                if (author.id == null) {
                    throw IllegalArgumentException("Author id can not be null.")
                }

                return Simple(
                    id = author.id!!,
                    name = author.name
                )
            }
        }
    }
}