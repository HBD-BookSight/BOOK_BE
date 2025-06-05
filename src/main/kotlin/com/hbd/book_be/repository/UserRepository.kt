package com.hbd.book_be.repository

import com.hbd.book_be.domain.User
import com.hbd.book_be.enums.UserRole
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun existsByName(name: String): Boolean
}
