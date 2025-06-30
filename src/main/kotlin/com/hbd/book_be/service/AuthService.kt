package com.hbd.book_be.service

import com.hbd.book_be.dto.request.LoginRequest
import com.hbd.book_be.dto.response.LoginResponse
import com.hbd.book_be.dto.response.TokenRefreshResponse
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.repository.UserRepository
import com.hbd.book_be.security.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    fun login(loginRequest: LoginRequest): LoginResponse {
        val user = userRepository.findByName(loginRequest.username)
            ?: throw NotFoundException("사용자를 찾을 수 없습니다: ${loginRequest.username}")

        if (!passwordEncoder.matches(loginRequest.password, user.password)) {
            throw IllegalArgumentException("잘못된 비밀번호입니다.")
        }

        val accessToken = jwtTokenProvider.generateAccessToken(
            userId = user.id!!,
            username = user.name,
            role = user.role.name
        )

        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id!!)

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = LoginResponse.UserInfo(
                id = user.id!!,
                username = user.name,
                role = user.role.name
            )
        )
    }

    fun refreshToken(refreshToken: String): TokenRefreshResponse {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.")
        }

        val userId = jwtTokenProvider.getUserIdFromToken(refreshToken)
        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundException("사용자를 찾을 수 없습니다: $userId") }

        val newAccessToken = jwtTokenProvider.generateAccessToken(
            userId = user.id!!,
            username = user.name,
            role = user.role.name
        )

        return TokenRefreshResponse(accessToken = newAccessToken)
    }
}
