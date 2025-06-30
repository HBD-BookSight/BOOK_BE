package com.hbd.book_be.dto.request

import com.hbd.book_be.domain.common.UrlInfo
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

data class ContentsCreateRequest(
    @field:Schema(
        description = "콘텐츠 생성자 ID",
        required = true,
        format = "int64",
        example = "1"
    )
    val creatorId: Long,

    @field:Schema(
        description = "콘텐츠 제목 (선택 사항)",
        required = false,
        example = "좋은 책 추천"
    )
    val title: String?,

    @field:Schema(
        description = "콘텐츠 이미지 URL (선택 사항)",
        required = false,
        example = "https://example.com/image.jpg"
    )
    val image: String?,

    @field:Schema(
        description = "콘텐츠 설명 (선택 사항)",
        required = false,
        example = "이 책은 정말 좋은 책입니다."
    )
    val description: String?,

    @field:Schema(
        description = "콘텐츠 메모 (선택 사항)",
        required = false,
        example = "개인적인 생각이나 메모"
    )
    val memo: String?,

    @field:ArraySchema(
        schema = Schema(
            description = "관련 URL 목록",
            required = true,
            implementation = UrlInfo::class
        ),
        arraySchema = Schema(
            type = "array",
            defaultValue = "[{\"url\": \"https://www.youtube.com/watch?v=movie123\",\n \"type\": \"youtube\"}]"
        )
    )
    val urls: List<UrlInfo>,

    @field:ArraySchema(
        schema = Schema(
            description = "태그 목록",
            required = false,
            type = "string"
        ),
        arraySchema = Schema(
            type = "array",
            defaultValue = "[]",
            example = "[\"소설\", \"추천도서\"]"
        )
    )
    val tagList: List<String> = emptyList(),

    @field:ArraySchema(
        schema = Schema(
            description = "관련 책 ISBN 목록",
            required = false,
            type = "string"
        ),
        arraySchema = Schema(
            type = "array",
            defaultValue = "[]",
            example = "[\"9788954429559\", \"9788937460739\"]"
        )
    )
    val bookIsbnList: List<String> = emptyList()
)
