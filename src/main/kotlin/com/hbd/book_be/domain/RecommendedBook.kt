package com.hbd.book_be.domain

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "recommended_book",
    indexes = [
        Index(name = "idx_recommended_book_created_at", columnList = "created_at"),
        Index(name = "idx_recommended_book_recommended_date", columnList = "recommended_date")
    ]
)
class RecommendedBook(
    @Id
    var isbn: String,

    @MapsId(value = "isbn")
    @OneToOne(targetEntity = Book::class, fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn")
    var book: Book,

    @Column(name = "recommended_date", nullable = false)
    val recommendedDate: LocalDate,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
