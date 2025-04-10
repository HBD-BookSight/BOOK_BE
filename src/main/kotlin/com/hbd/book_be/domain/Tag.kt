package com.hbd.book_be.domain

import com.hbd.book_be.domain.core.BaseTimeEntity
import jakarta.persistence.*


@Entity
@Table(name = "tag",
    indexes = [
        Index(name = "idx_tag_name", columnList = "name"),
    ])
class Tag (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var name: String,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tag", cascade = [CascadeType.ALL], orphanRemoval = true)
    var tagContentsList: MutableList<TagContents> = mutableListOf(),

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tag", cascade = [CascadeType.ALL], orphanRemoval = true)
    var tagEventsList: MutableList<TagEvent> = mutableListOf()

) : BaseTimeEntity(){

    fun getContentsList(): List<Contents> {
        return tagContentsList.map { it.contents }
    }

    fun addContentsList(contents: Contents) {
        val addedTagContents = TagContents(
            tag = this,
            contents = contents
        )
        contents.tagContentsList.add(addedTagContents)
        this.tagContentsList.add(addedTagContents)
    }

    fun getEventsList(): List<Event> {
        return tagEventsList.map { it.event }
    }

    fun addEventsList(event: Event) {
        val addedTagEvent= TagEvent(
            tag = this,
            event = event
        )
        event.tagEventsList.add(addedTagEvent)
        this.tagEventsList.add(addedTagEvent)
    }
}


