package com.hbd.book_be.external.kakao

data class KakaoApiResponse(
    val meta: Meta,
    val documents: List<Document>
) {
    data class Meta(
        val totalCount: Int,
        val pageableCount: Int,
        val isEnd: Boolean
    )

    data class Document(
        val title: String,
        val contents: String,
        val url: String,

        //ISBN10 또는 ISBN13 중 하나 이상 포함, 두 값이 모두 제공될 경우 공백(' ')
        val isbn: String,
        //[YYYY]-[MM]-[DD]T[hh]:[mm]:[ss].000+[tz]
        val datetime: String,
        val authors: List<String>,
        val publisher: String,
        val translators: List<String>,
        val price: Int,
        val salePrice: Int,
        val thumbnail: String,

        // 판매 상태 (정상, 품절, 절판 등), 상황에 따라 변동 가능성이 있으므로 문자열 처리 지양
        val status: String
    )
}