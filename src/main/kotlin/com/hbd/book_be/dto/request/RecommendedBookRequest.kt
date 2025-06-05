package com.hbd.book_be.dto.request

import io.swagger.v3.oas.annotations.media.Schema

data class RecommendedBookCreateRequest(
    @field:Schema(description = "추천할 도서의 ISBN", example = "9791168473690")
    val isbn: String,
)
