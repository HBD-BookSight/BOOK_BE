package com.hbd.book_be.domain

import jakarta.persistence.*
import java.io.Serializable

data class BookContentsId(
    var contents: Contents? = null,
    var book: Book? = null
) : Serializable

@Entity
@IdClass(BookContentsId::class)
@Table(
    name = "book_contents",
    indexes = [
        Index(name = "idx_book_contents_isbn", columnList = "isbn"),
        Index(name = "idx_book_contents_contents_id", columnList = "contents_id"),
    ]
)
class BookContents(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn", nullable = false, referencedColumnName = "isbn")
    var book: Book,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contents_id", nullable = false, referencedColumnName = "id")
    var contents: Contents
)
