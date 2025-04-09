package com.hbd.book_be.domain

import com.hbd.book_be.domain.core.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "author",
    indexes = [
        Index(name = "idx_author_koNm", columnList = "koNm"),
        Index(name = "idx_author_enNm", columnList = "enNm")
    ]
)
class Author(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    var id: Long? = null,

    @Column(name = "koNm") var koNm: String,

    @Column(name = "enNm") var enNm: String,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "author", cascade = [CascadeType.ALL], orphanRemoval = true)
    var bookList: MutableList<Book> = mutableListOf(),
) : BaseTimeEntity()