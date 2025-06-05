package com.hbd.book_be.service

import com.hbd.book_be.domain.User
import com.hbd.book_be.enums.UserRole
import com.hbd.book_be.repository.UserRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DataInitializerService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    private val logger = LoggerFactory.getLogger(DataInitializerService::class.java)

    @PostConstruct
    @Transactional
    fun initializeData() {
        try {
            logger.info("=== 데이터 초기화 시작 ===")

            createAdminUserIfNotExists()

            logger.info("=== 데이터 초기화 완료 ===")

        } catch (e: Exception) {
            logger.error("❌ 데이터 초기화 중 오류 발생: ${e.message}", e)
            throw e
        }
    }

    private fun createAdminUserIfNotExists() {
        val adminExists = userRepository.existsByName("admin")

        if (!adminExists) {
            val rawPassword = "test1234"
            val encodedPassword = passwordEncoder.encode(rawPassword)

            val adminUser = User(
                password = encodedPassword,
                name = "admin",
                role = UserRole.ADMIN
            )

            val savedUser = userRepository.save(adminUser)
            logger.info("✅ 관리자 계정이 생성되었습니다: ${savedUser.name} (ID: ${savedUser.id}) [BCrypt 암호화 적용]")
            logger.debug("🔐 Raw Password: $rawPassword -> Encoded: ${encodedPassword.take(20)}...")
        } else {
            logger.info("ℹ️ 관리자 계정이 이미 존재합니다")
        }
    }
}
