package com.hbd.book_be.aop

import com.hbd.book_be.util.AuthUtils
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Aspect
@Component
class AdminRoleAspect(
    private val authUtils: AuthUtils
) {

    private val log = LoggerFactory.getLogger(AdminRoleAspect::class.java)

    @Before("@annotation(com.hbd.book_be.annotation.RequireAdminRole)")
    fun checkAdminRole(joinPoint: JoinPoint) {
        val methodName = "${joinPoint.signature.declaringTypeName}.${joinPoint.signature.name}"

        log.debug("관리자 권한 체크 시작: $methodName")

        try {
            if (!authUtils.isCurrentUserAdmin()) {
                log.warn("권한 거부: $methodName - 관리자 권한이 필요합니다.")
                throw IllegalAccessException("관리자 권한이 필요합니다.")
            }

            log.debug("관리자 권한 체크 통과: $methodName")
        } catch (e: IllegalStateException) {
            // AuthUtils에서 토큰 관련 에러가 발생한 경우
            log.warn("인증 실패: $methodName - ${e.message}")
            throw IllegalAccessException("인증이 필요합니다.")
        }
    }
}