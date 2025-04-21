package com.hbd.book_be.dto.request

import com.hbd.book_be.enums.ContentType

data class ContentsSearchRequest(
    val type: ContentType? = null,
)
