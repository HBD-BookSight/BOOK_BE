package com.hbd.book_be.dto.request

import com.hbd.book_be.enums.SortType

data class KakaoBookRequest(
    val query: String = "",
    val sort: SortType = SortType.ACCURACY,
    val page: Int? = 1,
    val size: Int? = 10,

    // 검색 필드 제한
    val target: String? = null
)