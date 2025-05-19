package com.hbd.book_be.external.kakao

import java.time.LocalDateTime

data class KakaoBookDto(
    val isbn10: String?,
    val isbn13: String?,
    val isIsbn10Exist: Boolean,
    val isIsbn13Exist: Boolean,
    val title: String,
    val summary: String,
    val publishedDate: LocalDateTime,
    val titleImage: String?,
    val authorList: List<String>,
    val translator: List<String>?,
    val price: Int?,
    val publisher: String,
) {
    companion object {
        fun fromKakaoApi(
            document: KakaoApiResponse.Document,
            isIsbn10Exist: Boolean,
            isIsbn13Exist: Boolean
        ): KakaoBookDto {
            val publishedDate = LocalDateTime.parse(document.datetime.substring(0, 19))
            val isbnList = document.isbn.split(" ")

            // ISBN10, ISBN13 체크 로직
            val isbn10 = isbnList.find { it.length == 10 }
            val isbn13 = isbnList.find { it.length == 13 }

            return KakaoBookDto(
                isbn10 = isbn10,
                isbn13 = isbn13,
                isIsbn10Exist = isIsbn10Exist,
                isIsbn13Exist = isIsbn13Exist,
                title = document.title,
                summary = document.contents,
                publishedDate = publishedDate,
                titleImage = document.thumbnail,
                authorList = document.authors,
                translator = document.translators,
                price = document.price,
                publisher = document.publisher,
            )
        }
    }
}
