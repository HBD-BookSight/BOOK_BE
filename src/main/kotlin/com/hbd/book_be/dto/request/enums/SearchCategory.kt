package com.hbd.book_be.dto.request.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Field to search for books")
enum class SearchCategory {
    @field:Schema(description = "search by book title")
    Title,

    @field:Schema(description = "search by author name")
    Author,

    @field:Schema(description = "search by publisher name")
    Publisher,
}