package com.hbd.book_be.domain

import java.io.Serializable
import jakarta.persistence.*

data class BookContentsId(
    var contents: Contents,
    var book: Book
) : Serializable

@Entity
@IdClass(BookContentsId::class)
@Table(name = "book_contents")
class BookContents(
    @Id
    @ManyToOne
    @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    var book: Book,

    @Id
    @ManyToOne
    @JoinColumn(name = "contents_id", referencedColumnName = "id")
    var contents: Contents
)
