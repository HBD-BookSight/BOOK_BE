package com.hbd.book_be.dto.request

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader

data class BookDetailRequest(
    @PathVariable
    val isbn: String,
    
    @RequestHeader("X-SOURCE-PATH")
    val sourcePath: String? = null,
    
    @RequestHeader("X-SOURCE-KEYWORD")
    val sourceKeyword: String? = null
) 