package com.hbd.book_be.dto.request

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema

data class ContactCreateRequest(
    @field:Parameter(
        description = "Contact 이름 (선택 사항)",
        schema = Schema(
            type = "string",
            example = "홍길동"
        ),
        required = false,
    )
    val name: String?,

    @field:Parameter(
        description = "이메일 주소",
        schema = Schema(
            type = "string",
            example = "hong@example.com"
        ),
        required = true
    )
    val email: String,

    @field:Parameter(
        description = "문의 메시지",
        schema = Schema(
            type = "string",
            example = "안녕하세요. 문의사항이 있습니다."
        ),
        required = true
    )
    val message: String
)
