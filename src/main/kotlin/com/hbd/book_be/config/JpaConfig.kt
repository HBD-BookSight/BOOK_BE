package com.hbd.book_be.config

import com.hbd.book_be.domain.User
import com.hbd.book_be.domain.UserBuilder
import com.hbd.book_be.domain.UserRole
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@Configuration
@EnableJpaAuditing
class JpaConfig()