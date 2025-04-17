package com.hbd.book_be.domain

import jakarta.persistence.*
import java.io.Serializable

data class BookEventId(
    var event: Event? = null,
    var book: Book? = null
) : Serializable

@Entity
@IdClass(BookEventId::class)
@Table(
    name = "book_event",
    indexes = [
        Index(name = "idx_book_event_isbn", columnList = "isbn"),
        Index(name = "idx_book_event_event_id", columnList = "event_id"),
    ]
)
class BookEvent(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, referencedColumnName = "id")
    var event: Event,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn", nullable = false, referencedColumnName = "isbn")
    var book: Book
)
