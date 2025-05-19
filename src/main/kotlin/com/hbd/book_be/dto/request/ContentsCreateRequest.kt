package com.hbd.book_be.dto.request

import com.hbd.book_be.domain.common.UrlInfo
import io.swagger.v3.oas.annotations.media.Schema

data class ContentsCreateRequest(
    val creatorId: Long,
    val title: String?,
    val image: String?,
    val description: String?,
    val memo: String?,

    @field:Schema(defaultValue = "[]")
    val urls: List<UrlInfo>,

    @field:Schema(defaultValue = "[]")
    val tagList: List<String> = emptyList(),

    @field:Schema(defaultValue = "[]")
    val bookIsbnList: List<String> = emptyList()
)
