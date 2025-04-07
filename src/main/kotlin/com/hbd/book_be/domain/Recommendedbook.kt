package com.hbd.book_be.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.LocalDateTime

@Embeddable
data class RecommendedBook(
    @Column(name = "recommended_date", nullable = false)
    val recommendedDate: LocalDateTime,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
