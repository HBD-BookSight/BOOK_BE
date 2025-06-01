package com.hbd.book_be.dto.request.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Direction for sorting results.")
enum class SortDirection {
    @field:Schema(description = "Ascending order")
    asc,
    @field:Schema(description = "Descending order")
    desc
} 