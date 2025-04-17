package com.hbd.book_be.repository

import com.hbd.book_be.domain.DiscoveryContents
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface DiscoveryContentsRepositoryCustom {
    fun findContentsWithConditions(pageable: Pageable): Page<DiscoveryContents>
}