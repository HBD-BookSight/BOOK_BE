package com.hbd.book_be.repository

import com.hbd.book_be.domain.Tag
import org.springframework.data.jpa.repository.JpaRepository

interface TagRepository : JpaRepository<Tag, Long> {

    fun findByName(name: String): Tag?
}