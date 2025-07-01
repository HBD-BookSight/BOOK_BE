package com.hbd.book_be.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class LoginResponse(
    @field:Schema(
        description = "액세스 토큰",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    val accessToken: String,

    @field:Schema(
        description = "리프레시 토큰",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    val refreshToken: String,

    @field:Schema(
        description = "토큰 타입",
        example = "Bearer"
    )
    val tokenType: String = "Bearer",

    @field:Schema(
        description = "액세스 토큰 만료 시간 (초)",
        example = "3600"
    )
    val expiresIn: Long = 3600,

    @field:Schema(
        description = "사용자 정보"
    )
    val user: UserInfo
) {
    data class UserInfo(
        @field:Schema(description = "사용자 ID", example = "1")
        val id: Long,
        
        @field:Schema(description = "사용자명", example = "john_doe")
        val username: String,
        
        @field:Schema(description = "사용자 역할", example = "USER")
        val role: String
    )
}

data class TokenRefreshResponse(
    @field:Schema(
        description = "새로운 액세스 토큰",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    val accessToken: String,

    @field:Schema(
        description = "토큰 타입",
        example = "Bearer"
    )
    val tokenType: String = "Bearer",

    @field:Schema(
        description = "액세스 토큰 만료 시간 (초)",
        example = "3600"
    )
    val expiresIn: Long = 3600
)
