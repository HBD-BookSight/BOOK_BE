package com.hbd.book_be.repository

import com.hbd.book_be.domain.Contents
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ContentsRepository : JpaRepository<Contents, Long> {
    @Query("select contents from Contents contents where contents.deletedAt IS NULL")
    fun findAllActive(pageable: Pageable): Page<Contents>
}