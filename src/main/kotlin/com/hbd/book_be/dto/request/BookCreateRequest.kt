package com.hbd.book_be.dto.request

import java.time.LocalDateTime

data class BookCreateRequest(
    val isbn: String,
    val title: String,
    val summary: String,
    val publishedDate: LocalDateTime,
    val detailUrl: String?,
    val translator: String? = null,
    val price: Int? = null,
    val titleImage: String? = null,

    val authorIdList: List<Long> = listOf(),
    val authorNameList: List<String> = listOf(),
    val publisherId: Long? = null,
    val publisherName: String? = null
)
