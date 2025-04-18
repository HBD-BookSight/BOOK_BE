package com.hbd.book_be.domain

import com.hbd.book_be.domain.common.UrlInfo
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

    @Column(name = "eng_name", nullable = true, unique = true)
    var engName: String?,

    @Column(name = "logo")
    var logo: String?,

    @Convert(converter = UrlInfo.Converter::class)
    @Column(name = "urls", columnDefinition = "json")
    var urls: MutableList<UrlInfo> = mutableListOf(),

    @Column(name = "description", length = 2000)
    var description: String?,

    @Column(name = "is_official", nullable = false)
    var isOfficial: Boolean = false,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "publisher", cascade = [CascadeType.ALL], orphanRemoval = true)
    val publisherTitleBooksList: MutableList<PublisherTitleBook> = mutableListOf(),

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "publisher", cascade = [CascadeType.ALL])
    var bookList: MutableList<Book> = mutableListOf(),

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "publisher", cascade = [CascadeType.ALL], orphanRemoval = true)
    var tagPublisherList: MutableList<TagPublisher> = mutableListOf(),

    ) : BaseTimeEntity() {

    fun clearTags() {
        tagPublisherList.forEach { tagPublisher ->
            tagPublisher.tag.tagPublisherList.remove(tagPublisher)
        }
        tagPublisherList.clear()
    }

    fun addTag(tag: Tag) {
        val addedTag = TagPublisher(tag = tag, publisher = this)
        this.tagPublisherList.add(addedTag)
        tag.tagPublisherList.add(addedTag)
    }
}