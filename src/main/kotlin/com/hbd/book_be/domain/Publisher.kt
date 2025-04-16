package com.hbd.book_be.domain

import com.hbd.book_be.domain.core.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "publisher",
    indexes = [
        Index(name = "idx_publisher_is_official_name", columnList = "is_official, name")
    ]
)
class Publisher(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    var id: Long? = null,

    @Column(name = "name", nullable = false, unique = true)
    var name: String,

    @Column(name = "logo")
    var logo: String? = null,

    @Column(name = "link", columnDefinition = "json")
    var link: String? = null,

    @Column(name = "description", length = 2000)
    var description: String? = null,

    @Column(name = "is_official", nullable = false)
    var isOfficial: Boolean = false,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "publisher", cascade = [CascadeType.ALL], orphanRemoval = true)
    val publisherTitleBooksList: MutableList<PublisherTitleBook> = mutableListOf(),

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "publisher", cascade = [CascadeType.ALL], orphanRemoval = true)
    var bookList: MutableList<Book> = mutableListOf()

) : BaseTimeEntity()