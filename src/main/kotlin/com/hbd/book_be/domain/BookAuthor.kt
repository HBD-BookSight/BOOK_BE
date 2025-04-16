package com.hbd.book_be.domain

import jakarta.persistence.*
import java.io.Serializable

data class BookAuthorId(
    val book: Book? = null,
    val author: Author? = null
): Serializable

@Entity
@IdClass(BookAuthorId::class)
@Table(
    name = "book_author",
    indexes = [
        Index(name = "idx_book_auth_isbn", columnList = "isbn"),
        Index(name = "idx_book_author_id", columnList = "author_id")
    ]
)
class BookAuthor(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn", nullable = false, referencedColumnName = "isbn")
    val book: Book,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false, referencedColumnName = "id")
    val author: Author
)