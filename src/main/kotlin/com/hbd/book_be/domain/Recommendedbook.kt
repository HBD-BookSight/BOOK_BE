package com.hbd.book_be.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "recommended_book")
class RecommendedBook(
    @Id
    var isbn: String,

    @MapsId(value="isbn")
    @OneToOne(targetEntity = Book::class, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name="isbn")
    var book: Book? = null,

    @Column(name = "recommended_date", nullable = false)
    val recommendedDate: LocalDateTime,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
