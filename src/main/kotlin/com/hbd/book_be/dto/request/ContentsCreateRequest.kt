package com.hbd.book_be.dto.request

import com.hbd.book_be.domain.enums.ContentType

data class ContentsCreateRequest(
    val type: ContentType,
    val url: String,
    val image: String?,
    val description: String?,
    val memo: String?,
    val creatorId: Long,
    val tagList: List<String> = emptyList(),
    val bookIsbnList: List<String> = emptyList()
)
