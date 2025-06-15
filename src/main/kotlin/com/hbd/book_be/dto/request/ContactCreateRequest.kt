package com.hbd.book_be.dto.request

import io.swagger.v3.oas.annotations.media.Schema

data class ContactCreateRequest(
    @field:Schema(
        description = "Contact 이름 (선택 사항)",
        example = "홍길동",
        required = false,
    )
    val name: String?,

    @field:Schema(
        description = "이메일 주소",
        example = "hong@example.com",
        required = true
    )
    val email: String,

    @field:Schema(
        description = "문의 메시지",
        example = "안녕하세요. 문의사항이 있습니다.",
        required = true
    )
    val message: String
)
