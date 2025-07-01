package com.hbd.book_be.dto.request

import io.swagger.v3.oas.annotations.media.Schema

data class LoginRequest(
    @field:Schema(
        description = "사용자명",
        required = true,
        example = "admin"
    )
    val username: String,

    @field:Schema(
        description = "비밀번호",
        required = true,
        example = "test1234"
    )
    val password: String
)

data class RefreshTokenRequest(
    @field:Schema(
        description = "리프레시 토큰",
        required = true,
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    val refreshToken: String
)
