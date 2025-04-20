package com.hbd.book_be.domain

import com.hbd.book_be.domain.core.BaseTimeEntity
import jakarta.persistence.*
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

    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "summary", nullable = false)
    var summary: String,

    @Column(name = "published_date", nullable = false)
    var publishedDate: LocalDateTime,

    @Column(name = "detail_url")
    var detailUrl: String?,

    @Column(name = "translator")
    var translator: String?,

    @Column(name = "price")
    var price: Int?,

    @Column(name = "title_image")
    var titleImage: String?,

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
        val addedBookContents = BookContents(book = this, contents = contents)

        this.bookContentsList.add(addedBookContents)
        contents.bookContentsList.add(addedBookContents)
    }

    fun getEventList(): List<Event> {
        return this.bookEventList.map { it.event }
    }

    fun addEvent(event: Event) {
        val addedBookEvent = BookEvent(book = this, event = event)
        this.bookEventList.add(addedBookEvent)
        event.bookEventList.add(addedBookEvent)
    }

    fun addAuthor(author: Author) {
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

