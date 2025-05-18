package com.hbd.book_be.external.kakao

import java.time.LocalDateTime

data class KakaoBookDto(
    val isExist: List<Boolean>,
    val isbn: String,
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
        fun fromKakaoApi(document: KakaoApiResponse.Document, isExist: List<Boolean>): KakaoBookDto {
            val publishedDate = LocalDateTime.parse(document.datetime.substring(0, 19))
            return KakaoBookDto(
                isExist = isExist,
                isbn = document.isbn.split(" ").first(),
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