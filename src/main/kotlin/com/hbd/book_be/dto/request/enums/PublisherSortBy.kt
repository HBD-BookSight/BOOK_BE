package com.hbd.book_be.dto.request.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description="Filed to sort publishers by")
enum class PublisherSortBy(val value: String) {
    @field:Schema(description = "Sort by publisher name")
    Name("name"),

    @field:Schema(description = "Sort by publisher Created At")
    CreatedAt("createdAt"),
}