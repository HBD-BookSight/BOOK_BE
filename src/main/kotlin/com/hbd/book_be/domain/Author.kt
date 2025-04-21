package com.hbd.book_be.domain

import com.hbd.book_be.domain.core.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "author",
    indexes = [
        Index(name = "idx_author_name", columnList = "name"),
        Index(name = "idx_author_is_official", columnList = "is_official"),
    ]
)
class Author(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    var id: Long? = null,

    @Column(name = "name")
    var name: String,

    @Column(name = "description")
    var description: String?,

    @Column(name = "profile")
    var profile: String?,

    @Column(name = "is_official")
    var isOfficial: Boolean = false,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "author")
    var bookAuthorList: MutableList<BookAuthor> = mutableListOf(),
) : BaseTimeEntity()