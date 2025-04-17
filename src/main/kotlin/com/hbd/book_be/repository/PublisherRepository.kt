package com.hbd.book_be.repository

import com.hbd.book_be.domain.Publisher
import org.springframework.data.jpa.repository.JpaRepository

interface PublisherRepository : JpaRepository<Publisher, Long>{
    fun findByName(name: String): Publisher?
}