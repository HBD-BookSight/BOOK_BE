package com.hbd.book_be.domain

import com.hbd.book_be.domain.core.BaseTimeEntity
import com.hbd.book_be.domain.enums.ContentType
import jakarta.persistence.*

@Entity
@Table(
    name = "contents",
    indexes = [
        Index(name = "idx_contents_type", columnList = "type"),
        Index(name = "idx_contents_creator_id", columnList = "creator_id"),
        Index(name = "idx_contents_created_at", columnList = "created_at")
    ]
)
class Contents(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: ContentType,

    @Column(name = "url", nullable = false)
    var url: String,

    @Column(name = "image")
    var image: String? = null,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "memo")
    var memo: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    var creator: User,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contents", cascade = [CascadeType.ALL], orphanRemoval = true)
    val bookContentsList: MutableList<BookContents> = mutableListOf(),

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contents", cascade = [CascadeType.ALL], orphanRemoval = true)
    val tagContentsList: MutableList<TagContents> = mutableListOf()
) : BaseTimeEntity() {

    fun getTagList(): List<Tag> {
        return tagContentsList.map { it.tag }
    }

    fun addTag(tag: Tag) {
        val addedTagContents = TagContents(
            tag = tag,
            contents = this
        )
        this.tagContentsList.add(addedTagContents)
        tag.tagContentsList.add(addedTagContents)
    }

    fun getBookList(): List<Book> {
        return bookContentsList.map { it.book }
    }

    fun addBook(book: Book) {
        val addedTagContents = BookContents(
            book = book,
            contents = this
        )
        this.bookContentsList.add(addedTagContents)
        book.bookContentsList.add(addedTagContents)
    }
}
