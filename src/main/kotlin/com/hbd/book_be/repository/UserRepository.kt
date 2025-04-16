package com.hbd.book_be.repository

import com.hbd.book_be.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>