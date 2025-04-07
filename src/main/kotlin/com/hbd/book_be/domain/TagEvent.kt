package com.hbd.book_be.domain

import jakarta.persistence.*
import java.io.Serializable

data class TagEventId(
    val tag: Tag,
    val event: Event
) : Serializable

@Entity
@Table(name = "tags")
@IdClass(TagEventId::class)
class TagEvent(
    @Id
    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false, referencedColumnName = "id")
    val tag: Tag,

    @Id
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false, referencedColumnName = "id")
    val event: Event
)
