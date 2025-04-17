package com.hbd.book_be.repository

import com.hbd.book_be.domain.DiscoveryContents
import org.springframework.data.jpa.repository.JpaRepository

interface DiscoveryContentsRepository : JpaRepository<DiscoveryContents, String>, DiscoveryContentsRepositoryCustom