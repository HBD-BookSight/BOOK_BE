package com.hbd.book_be.dto.request.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Field to sort books by.")
enum class BookSortBy {
    @field:Schema(description = "Sort by book title")
    title,
    @field:Schema(description = "Sort by publication date")
    publishedDate
} 