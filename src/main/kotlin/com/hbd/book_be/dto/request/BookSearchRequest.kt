package com.hbd.book_be.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.bind.annotation.RequestParam

data class BookSearchRequest(
    @Schema(nullable = true, required = false, defaultValue = "")
    @RequestParam(required = false)
    val keyword: String? = null,

    @Schema(defaultValue = "0", example = "0")
    @RequestParam(defaultValue = "0")
    val page: Int = 0,

    @Schema(defaultValue = "10", example = "10")
    @RequestParam(defaultValue = "10")
    val limit: Int = 10,

    @Schema(defaultValue = "publishedDate", example = "publishedDate")
    @RequestParam(defaultValue = "publishedDate")
    val orderBy: String = "publishedDate",

    @Schema(defaultValue = "desc", example = "desc")
    @RequestParam(defaultValue = "desc")
    val direction: String = "desc"
)