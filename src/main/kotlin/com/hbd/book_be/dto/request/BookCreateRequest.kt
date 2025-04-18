package com.hbd.book_be.dto.request

import io.swagger.v3.oas.annotations.media.Schema
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

    @field:Schema(defaultValue = "[]")
    val authorIdList: List<Long> = listOf(),

    @field:Schema(defaultValue = "[]")
    val authorNameList: List<String> = listOf(),
    val publisherId: Long? = null,
    val publisherName: String? = null
)
