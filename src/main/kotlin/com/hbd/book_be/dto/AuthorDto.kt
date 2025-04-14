package com.hbd.book_be.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.hbd.book_be.domain.Author

data class AuthorDto(
    val id: Long,
    @JsonProperty("koreanName") val koNm: String? = null,
    @JsonProperty("englishName") val enNm: String? = null,
) {
    companion object {
        fun fromEntity(author: Author): AuthorDto {
            if (author.id == null) {
                throw IllegalArgumentException("Author id can not be null.")
            }

            return AuthorDto(
                id = author.id!!, koNm = author.koNm, enNm = author.enNm
            )
        }
    }
}