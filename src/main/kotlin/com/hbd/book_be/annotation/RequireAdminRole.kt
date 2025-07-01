package com.hbd.book_be.annotation

/**
 * 관리자 권한이 필요한 메서드에 적용하는 어노테이션
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireAdminRole
