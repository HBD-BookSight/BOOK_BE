package com.hbd.book_be.dto.request

data class PageRequest(
    val page: Int = 0,
    val limit: Int = 10,
    val orderBy: String = "publishedDate",
    val direction: String = "desc"
)
