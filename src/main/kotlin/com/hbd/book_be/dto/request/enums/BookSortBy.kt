package com.hbd.book_be.dto.request.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Field to sort books by.")
enum class BookSortBy(val value: String) {
    @field:Schema(description = "Sort by book title")
    Title("title"),

    @field:Schema(description = "Sort by publication date")
    PublishedDate("publishedDate")
}