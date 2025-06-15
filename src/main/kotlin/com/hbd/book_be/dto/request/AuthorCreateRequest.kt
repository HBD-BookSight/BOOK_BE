package com.hbd.book_be.dto.request

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

data class AuthorCreateRequest(
    @field:Parameter(
        description = "작가명",
        required = true,
        schema = Schema(
            type = "string",
            example = "김영하"
        )
    )
    val name: String,
    
    @field:Parameter(
        description = "작가 소개 및 설명",
        required = false,
        schema = Schema(
            type = "string",
            example = "한국의 소설가로, 대표작으로는 '살인자의 기억법', '빛의 과거' 등이 있다."
        )
    )
    val description: String? = null,
    
    @field:Parameter(
        description = "작가 프로필 이미지 URL",
        required = false,
        schema = Schema(
            type = "string",
            example = "https://image.aladin.co.kr/author/1234.jpg"
        )
    )
    val profile: String? = null,

    @field:Parameter(
        description = "작가와 관련된 도서의 ISBN 목록",
        required = false
    )
    @field:ArraySchema(
        schema = Schema(type = "string"),
        arraySchema = Schema(
            type = "array",
            example = "[\"9791168473690\", \"9788937473845\"]",
            defaultValue = "[]"
        )
    )
    val bookIsdnList: List<String> = emptyList()
)