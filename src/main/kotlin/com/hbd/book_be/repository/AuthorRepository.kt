package com.hbd.book_be.repository

import com.hbd.book_be.domain.Author
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface AuthorRepository : JpaRepository<Author, Long> {
    fun findFirstByName(name: String): Optional<Author>
}