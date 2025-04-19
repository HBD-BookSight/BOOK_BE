package com.hbd.book_be.repository

import com.hbd.book_be.domain.DiscoveryContents
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface DiscoveryContentsRepository : JpaRepository<DiscoveryContents, String> {

    @Query("SELECT dc FROM DiscoveryContents dc ORDER BY dc.createdAt DESC")
    fun findRecentDiscoveryContents(pageable: Pageable): List<DiscoveryContents>
}