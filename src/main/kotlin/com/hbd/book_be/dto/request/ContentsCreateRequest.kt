package com.hbd.book_be.dto.request

import com.hbd.book_be.domain.common.UrlInfo
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

data class ContentsCreateRequest(
    @field:Parameter(
        description = "콘텐츠 생성자 ID",
        required = true,
        schema = Schema(
            type = "integer",
            format = "int64",
            example = "1"
        )
    )
    val creatorId: Long,
    
    @field:Parameter(
        description = "콘텐츠 제목 (선택 사항)",
        required = false,
        schema = Schema(
            type = "string",
            example = "좋은 책 추천"
        )
    )
    val title: String?,
    
    @field:Parameter(
        description = "콘텐츠 이미지 URL (선택 사항)",
        required = false,
        schema = Schema(
            type = "string",
            example = "https://example.com/image.jpg"
        )
    )
    val image: String?,
    
    @field:Parameter(
        description = "콘텐츠 설명 (선택 사항)",
        required = false,
        schema = Schema(
            type = "string",
            example = "이 책은 정말 좋은 책입니다."
        )
    )
    val description: String?,
    
    @field:Parameter(
        description = "콘텐츠 메모 (선택 사항)",
        required = false,
        schema = Schema(
            type = "string",
            example = "개인적인 생각이나 메모"
        )
    )
    val memo: String?,

    @field:Parameter(
        description = "관련 URL 목록",
        required = true
    )
    @field:ArraySchema(
        schema = Schema(implementation = UrlInfo::class),
        arraySchema = Schema(
            type = "array",
            defaultValue = "[]"
        )
    )
    val urls: List<UrlInfo>,

    @field:Parameter(
        description = "태그 목록",
        required = false
    )
    @field:ArraySchema(
        schema = Schema(type = "string"),
        arraySchema = Schema(
            type = "array",
            defaultValue = "[]",
            example = "[\"소설\", \"추천도서\"]"
        )
    )
    val tagList: List<String> = emptyList(),

    @field:Parameter(
        description = "관련 책 ISBN 목록",
        required = false
    )
    @field:ArraySchema(
        schema = Schema(type = "string"),
        arraySchema = Schema(
            type = "array",
            defaultValue = "[]",
            example = "[\"9788954429559\", \"9788937460739\"]"
        )
    )
    val bookIsbnList: List<String> = emptyList()
)
