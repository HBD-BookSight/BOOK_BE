package com.hbd.book_be.domain

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime

data class PublisherTitleBookId(
    var book: Book,
    var publisher: Publisher
) : Serializable

@Entity
@IdClass(PublisherTitleBookId::class)
@Table(name = "publisher_title_book")
class PublisherTitleBook(

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    val book: Book,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", referencedColumnName = "id")
    val publisher: Publisher,

    @Column(name = "`rank`", nullable = false)
    var rank: Int,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
