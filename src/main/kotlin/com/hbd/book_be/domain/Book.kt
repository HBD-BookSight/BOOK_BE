package com.hbd.book_be.domain

import com.hbd.book_be.domain.core.BaseTimeEntity
import jakarta.persistence.*
import java.time.*

@Entity
@Table(name = "book")
class Book internal constructor(
    builder: BookBuilder
) : BaseTimeEntity() {

    @Id
    @Column(name = "isbn", nullable = false, updatable = false)
    var isbn: String? = builder.isbn
        protected set

    @Column(name = "title", nullable = false)
    var title: String? = builder.title
        protected set

    @Column(name = "summary", nullable = false)
    var summary: String? = builder.summary
        protected set

    @Column(name = "published_date", nullable = false)
    var publishedDate: LocalDateTime? = builder.publishedDate
        protected set

    @Column(name = "detail_url")
    var detailUrl: String? = builder.detailUrl
        protected set

    @Column(name = "translator")
    var translator: String? = builder.translator
        protected set

    @Column(name = "price")
    var price: Int? = builder.price
        protected set

    @Column(name = "title_image")
    var titleImage: String? = builder.titleImage
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    var author: Author? = builder.author
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false)
    var publisher: Publisher? = builder.publisher
        protected set

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "book", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val bookContentsList: MutableList<BookContents> = mutableListOf()
    val bookContents : List<BookContents> get() = bookContentsList.toList()

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "book", cascade = [CascadeType.ALL], orphanRemoval = true)
    val publisherTitleBookList: MutableList<PublisherTitleBook> = mutableListOf()
    val publisherTitleBook : List<PublisherTitleBook> get() = publisherTitleBookList.toList()

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "book", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val bookEventList: MutableList<BookEvent> = mutableListOf()
    val bookEvent: List<BookEvent> get() = bookEventList.toList()

    @ElementCollection
    @CollectionTable(name = "recommended_book", joinColumns = [JoinColumn(name = "isbn")])
    val recommendedBooksList: MutableList<RecommendedBook> = mutableListOf()
    val recommendedBooks : List<RecommendedBook> get() = recommendedBooksList.toList()

}


class BookBuilder internal constructor() {
    var isbn: String? = null
    var title: String? = null
    var summary: String? = null
    var publishedDate: LocalDateTime? = null
    var detailUrl: String? = null
    var translator: String? = null
    var price: Int? = null
    var titleImage: String? = null
    var author: Author? = null
    var publisher: Publisher? = null

    fun build(): Book {
        require(!isbn.isNullOrBlank()) { "ISBN은 필수입니다." }
        require(!title.isNullOrBlank()) { "책 제목은 필수입니다." }
        require(!summary.isNullOrBlank()) { "책 요약은 필수입니다." }
        require(publishedDate != null) { "출간일은 필수입니다." }
        require(author != null) { "작가는 필수입니다." }
        require(publisher != null) { "출판사는 필수입니다." }

        return Book(this)
    }
}
