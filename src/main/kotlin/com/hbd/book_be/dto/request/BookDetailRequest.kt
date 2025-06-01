package com.hbd.book_be.dto.request

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader

data class BookDetailRequest(
    @field:Parameter(
        description = "책의 ISBN 번호. (필수)",
        required = true
    )
    @PathVariable
    val isbn: String,

    @field:Parameter(
        description = "요청 출처 경로. (선택 사항)",
        schema = Schema(nullable = true)
    )
    @RequestHeader("X-SOURCE-PATH")
    val sourcePath: String? = null,

    @field:Parameter(
        description = "요청 출처 키워드. (선택 사항)",
        schema = Schema(nullable = true)
    )
    @RequestHeader("X-SOURCE-KEYWORD")
    val sourceKeyword: String? = null
) 