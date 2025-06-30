package com.hbd.book_be.controller.api

import com.hbd.book_be.dto.request.LoginRequest
import com.hbd.book_be.dto.request.RefreshTokenRequest
import com.hbd.book_be.dto.response.LoginResponse
import com.hbd.book_be.dto.response.TokenRefreshResponse
import com.hbd.book_be.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Auth API", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @Operation(
        summary = "로그인",
        description = "사용자 인증 후 JWT 토큰을 발급합니다."
    )
    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        val loginResponse = authService.login(loginRequest)
        return ResponseEntity.ok(loginResponse)
    }

    @Operation(
        summary = "토큰 갱신",
        description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다."
    )
    @PostMapping("/refresh")
    fun refreshToken(@RequestBody refreshTokenRequest: RefreshTokenRequest): ResponseEntity<TokenRefreshResponse> {
        val tokenRefreshResponse = authService.refreshToken(refreshTokenRequest.refreshToken)
        return ResponseEntity.ok(tokenRefreshResponse)
    }
}
