package com.hbd.book_be.dto.request

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

data class AuthorCreateRequest(
    @field:Schema(
        description = "작가명",
        required = true,
        example = "김영하"
    )
    val name: String,
    
    @field:Schema(
        description = "작가 소개 및 설명",
        required = false,
        example = "한국의 소설가로, 대표작으로는 '살인자의 기억법', '빛의 과거' 등이 있다."
    )
    val description: String? = null,
    
    @field:Schema(
        description = "작가 프로필 이미지 URL",
        required = false,
        example = "https://image.aladin.co.kr/author/1234.jpg"
    )
    val profile: String? = null,

    @field:ArraySchema(
        schema = Schema(
            description = "작가와 관련된 도서의 ISBN 목록",
            required = false,
            type = "string"
        ),
        arraySchema = Schema(
            type = "array",
            example = "[\"9791168473690\", \"9788937473845\"]",
            defaultValue = "[]"
        )
    )
    val bookIsdnList: List<String> = emptyList()
)