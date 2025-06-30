package com.hbd.book_be.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider {

    @Value("\${jwt.secret:mySecretKey12345678901234567890123456789012345678901234567890}")
    private lateinit var secretKey: String

    @Value("\${jwt.access-token-expire-time:3600000}") // 1시간
    private val accessTokenExpireTime: Long = 3600000

    @Value("\${jwt.refresh-token-expire-time:604800000}") // 7일
    private val refreshTokenExpireTime: Long = 604800000

    private fun getSigningKey(): SecretKey {
        return Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    /**
     * Access Token 생성
     */
    fun generateAccessToken(userId: Long, username: String, role: String): String {
        val now = Date()
        val expireDate = Date(now.time + accessTokenExpireTime)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("username", username)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expireDate)
            .signWith(getSigningKey())
            .compact()
    }

    /**
     * Refresh Token 생성
     */
    fun generateRefreshToken(userId: Long): String {
        val now = Date()
        val expireDate = Date(now.time + refreshTokenExpireTime)

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(expireDate)
            .signWith(getSigningKey())
            .compact()
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    fun getUserIdFromToken(token: String): Long {
        val claims = getClaimsFromToken(token)
        return claims.subject.toLong()
    }

    /**
     * 토큰에서 사용자명 추출
     */
    fun getUsernameFromToken(token: String): String {
        val claims = getClaimsFromToken(token)
        return claims["username"] as String
    }

    /**
     * 토큰에서 역할 추출
     */
    fun getRoleFromToken(token: String): String {
        val claims = getClaimsFromToken(token)
        return claims["role"] as String
    }

    /**
     * 토큰으로 Authentication 객체   생성
     */
    fun getAuthentication(token: String): Authentication {
        val userId = getUserIdFromToken(token)
        val username = getUsernameFromToken(token)
        val role = getRoleFromToken(token)

        val authorities: Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority("ROLE_$role"))
        val principal = UserPrincipal(userId, username, role)

        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    /**
     * 토큰 유효성 검증 (기본)
     */
    fun validateToken(token: String): Boolean {
        return runCatching {
            val claims = getClaimsFromToken(token)
            !isTokenExpired(claims)
        }.getOrDefault(false)
    }

    /**
     * Refresh Token 유효성 검증 (만료 시간으로 구분)
     */
    fun validateRefreshToken(token: String): Boolean {
        return runCatching {
            val claims = getClaimsFromToken(token)
            val expiration = claims.expiration
            val now = Date()

            // Refresh Token은 더 긴 만료 시간을 가져야 함
            // 접근법: 만료까지 남은 시간이 1일 이상이면 Refresh Token으로 간주
            val timeLeft = expiration.time - now.time
            val oneDayInMillis = 24 * 60 * 60 * 1000L

            !isTokenExpired(claims) && timeLeft > oneDayInMillis
        }.getOrDefault(false)
    }

    /**
     * 토큰에서 Claims 추출
     */
    private fun getClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    /**
     * 토큰 만료 여부 확인
     */
    private fun isTokenExpired(claims: Claims): Boolean {
        return claims.expiration.before(Date())
    }

    /**
     * 토큰 만료 여부 확인 (토큰으로부터)
     */
    private fun isTokenExpired(token: String): Boolean {
        val claims = getClaimsFromToken(token)
        return isTokenExpired(claims)
    }
}
