package com.hbd.book_be.domain

import com.hbd.book_be.domain.core.AutoIdEntity
import jakarta.persistence.*

@Entity
@Table(name = "publisher")
class Publisher internal constructor(
    builder: PublisherBuilder,
) : AutoIdEntity() {

    @Column(name = "name", nullable = false)
    var name: String = requireNotNull(builder.name) { "출판사 이름은 필수입니다." }
        protected set

    @Column(name = "logo")
    var logo: String? = builder.logo
        protected set

    @Column(name = "link")
    var link: String? = builder.link
        protected set

    @Column(name = "description", length = 2000)
    var description: String? = builder.description
        protected set

    @Column(name = "is_official", nullable = false)
    var isOfficial: Boolean = builder.isOfficial
        protected set

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "publisher", cascade = [CascadeType.ALL], orphanRemoval = true)
    val publisherTitleBooksList: MutableList<PublisherTitleBook> = mutableListOf()
    val publisherTitleBooks: List<PublisherTitleBook> get() = publisherTitleBooksList.toList()
}

class PublisherBuilder internal constructor() {
    var name: String? = null
    var logo: String? = null
    var link: String? = null
    var description: String? = null
    var isOfficial: Boolean = false

    internal fun build(): Publisher {
        require(!name.isNullOrBlank()) { "출판사 이름은 필수입니다." }
        return Publisher(this)
    }
}
