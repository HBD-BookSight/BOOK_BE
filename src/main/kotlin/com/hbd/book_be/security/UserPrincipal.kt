package com.hbd.book_be.security

data class UserPrincipal(
    val userId: Long,
    val username: String,
    val role: String
) {
    fun hasRole(requiredRole: String): Boolean {
        return role == requiredRole
    }
}
