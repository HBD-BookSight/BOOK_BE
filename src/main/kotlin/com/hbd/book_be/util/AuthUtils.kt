package com.hbd.book_be.util

import com.hbd.book_be.domain.User
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.repository.UserRepository
import com.hbd.book_be.security.JwtTokenProvider
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Component
class AuthUtils(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {


    /**
     * 현재 인증된 사용자를 반환합니다.
     * Authorization 헤더에서 JWT 토큰을 추출하여 사용자 정보를 가져옵니다.
     * @throws NotFoundException 사용자를 찾을 수 없는 경우
     * @throws IllegalStateException 인증 정보가 올바르지 않은 경우
     */
    fun getCurrentUser(): User {
        val token = extractTokenFromRequest()
            ?: throw IllegalStateException("인증 토큰이 없습니다.")

        if (!jwtTokenProvider.validateToken(token)) {
            throw IllegalStateException("유효하지 않은 토큰입니다.")
        }

        val userId = jwtTokenProvider.getUserIdFromToken(token)
        return userRepository.findById(userId)
            .orElseThrow { NotFoundException("사용자를 찾을 수 없습니다: $userId") }
    }

    /**
     * 현재 인증된 사용자의 역할을 반환합니다.
     * 인증되지 않은 경우 null을 반환합니다.
     */
    fun getCurrentUserRole(): String? {
        return try {
            val token = extractTokenFromRequest() ?: return null
            if (!jwtTokenProvider.validateToken(token)) return null
            jwtTokenProvider.getRoleFromToken(token)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 현재 사용자가 ADMIN 권한을 가지고 있는지 확인합니다.
     */
    fun isCurrentUserAdmin(): Boolean {
        return getCurrentUserRole() == "ADMIN"
    }

    /**
     * HTTP 요청에서 JWT 토큰을 추출합니다.
     * @return JWT 토큰 또는 null
     */
    private fun extractTokenFromRequest(): String? {
        val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: return null

        val request = requestAttributes.request
        val bearerToken = request.getHeader("Authorization")

        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7) // "Bearer " 제거
        } else {
            null
        }
    }
}
