package com.hbd.book_be.domain

import jakarta.persistence.*
import java.time.*

@Embeddable
data class DiscoveryContent(
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
