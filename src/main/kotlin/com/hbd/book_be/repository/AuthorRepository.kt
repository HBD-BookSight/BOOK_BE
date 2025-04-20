package com.hbd.book_be.repository

import com.hbd.book_be.domain.Author
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface AuthorRepository : JpaRepository<Author, Long> {
    @Query("select author from Author author where author.name = :name AND author.deletedAt IS NULL")
    fun findFirstByName(name: String): Optional<Author>

    @Query("select author from Author author where author.isOfficial = true AND author.deletedAt IS NULL")
    fun findAllActiveOfficialAuthors(pageable: Pageable): Page<Author>
}