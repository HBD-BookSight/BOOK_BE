package com.hbd.book_be.external.kakao

import com.hbd.book_be.domain.Book
import com.hbd.book_be.dto.BookDto

data class KakaoBookDto(
    val isExist: Boolean,
    val bookDto: BookDto
) {
    companion object {
        fun fromEntity(book: Book, isExist: Boolean): KakaoBookDto {
            return KakaoBookDto(
                isExist = isExist,
                bookDto = BookDto.fromEntity(book)
            )
        }
    }
}
