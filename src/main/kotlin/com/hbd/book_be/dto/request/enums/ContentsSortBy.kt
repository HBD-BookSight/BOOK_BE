package com.hbd.book_be.dto.request.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Field to sort contents by.")
enum class ContentsSortBy(val value: String) {
    @field:Schema(description = "Sort by book title")
    Title("title"),

    @field:Schema(description = "Sort by Created At")
    CreatedAt("createdAt")
}