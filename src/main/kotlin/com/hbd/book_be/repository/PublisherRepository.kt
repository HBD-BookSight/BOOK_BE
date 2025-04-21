package com.hbd.book_be.repository

import com.hbd.book_be.domain.Publisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PublisherRepository : JpaRepository<Publisher, Long>{
    @Query("select p from Publisher p where p.name = :name AND p.deletedAt IS NULL")
    fun findByName(name: String): Publisher?

    @Query("select p from Publisher p where p.deletedAt IS NULL")
    fun findAllActive(pageable: Pageable): Page<Publisher>
}