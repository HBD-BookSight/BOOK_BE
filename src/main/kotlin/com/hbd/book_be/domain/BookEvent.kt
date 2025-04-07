package com.hbd.book_be.domain

import jakarta.persistence.*
import java.io.Serializable

data class BookEventId(
    var event: Event,
    var book: Book
) : Serializable

@Entity
@Table(name = "book_event")
@IdClass(BookEventId::class)
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
