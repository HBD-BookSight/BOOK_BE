package com.hbd.book_be.domain

import com.hbd.book_be.domain.converter.CommaListConverter
import com.hbd.book_be.domain.core.BaseTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(
    name = "book",
    indexes = [
        Index(name = "idx_book_title", columnList = "title"),
        Index(name = "idx_book_published_date", columnList = "published_date"),
        Index(name = "idx_book_deleted_at", columnList = "deleted_at")
    ]
)
class Book(
    @Id
    @Column(name = "isbn", nullable = false, updatable = false)
    var isbn: String,

    //length 최대 768 까지인 것을 확인 (265 보다 긴 title을 가진 외국도서들이 있음)
    @Column(name = "title", nullable = false, length = 700)
    var title: String,

    @JdbcTypeCode(SqlTypes.NCLOB)
    @Column(name = "summary", nullable = false) // OCI DB doesn't support 'TEXT'
    var summary: String,

    @Column(name = "published_date", nullable = false)
    var publishedDate: LocalDateTime,

    @JdbcTypeCode(SqlTypes.NCLOB)
    @Column(name = "detail_url") // OCI DB doesn't support 'TEXT'
    var detailUrl: String?,

    @Convert(converter = CommaListConverter::class)
    @Column(name = "translator", length = 1000)
    var translator: List<String>?,

    @Column(name = "price")
    var price: Int?,

    @JdbcTypeCode(SqlTypes.NCLOB)
    @Column(name = "title_image")
    var titleImage: String?,

    @Column(name = "status")
    var status: String?,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "book", cascade = [CascadeType.ALL], orphanRemoval = true)
    var bookAuthorList: MutableList<BookAuthor> = mutableListOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false)
    var publisher: Publisher,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "book", cascade = [CascadeType.ALL], orphanRemoval = true)
    var bookContentsList: MutableList<BookContents> = mutableListOf(),

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "book", cascade = [CascadeType.ALL], orphanRemoval = true)
    var bookEventList: MutableList<BookEvent> = mutableListOf(),

    ) : BaseTimeEntity() {

    fun getContentsList(): List<Contents> {
        return this.bookContentsList.map { it.contents }
    }

    fun addContents(contents: Contents) {
        if (this.bookContentsList.any { it.contents.id == contents.id }) {
            return
        }

        val addedBookContents = BookContents(book = this, contents = contents)

        this.bookContentsList.add(addedBookContents)
        contents.bookContentsList.add(addedBookContents)
    }

    fun getEventList(): List<Event> {
        return this.bookEventList.map { it.event }
    }

    fun addEvent(event: Event) {
        if (this.bookEventList.any { it.event.id == event.id }) {
            return
        }

        val addedBookEvent = BookEvent(book = this, event = event)
        this.bookEventList.add(addedBookEvent)
        event.bookEventList.add(addedBookEvent)
    }

    fun addAuthor(author: Author) {
        if (this.bookAuthorList.any { it.author.id == author.id }) {
            return
        }

        val bookAuthor = BookAuthor(book = this, author = author)
        this.bookAuthorList.add(bookAuthor)
        author.bookAuthorList.add(bookAuthor)
    }

    fun removeAuthor(author: Author) {
        val targetBookAuthor = this.bookAuthorList.find { it.author.id == author.id }
        if (targetBookAuthor == null) {
            return
        }

        this.bookAuthorList.remove(targetBookAuthor)
        targetBookAuthor.author.bookAuthorList.remove(targetBookAuthor)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Book) return false
        return isbn == other.isbn
    }

    override fun hashCode(): Int {
        return isbn.hashCode()
    }
}

