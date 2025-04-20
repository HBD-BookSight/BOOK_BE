package com.hbd.book_be.domain

import jakarta.persistence.*
import java.io.Serializable

data class TagPublisherId(
    var tag: Tag? = null,
    var publisher: Publisher? = null
) : Serializable

@Entity
@Table(name = "tag_publisher")
@IdClass(TagPublisherId::class)
class TagPublisher(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false, referencedColumnName = "id")
    var tag: Tag,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false, referencedColumnName = "id")
    var publisher: Publisher
)