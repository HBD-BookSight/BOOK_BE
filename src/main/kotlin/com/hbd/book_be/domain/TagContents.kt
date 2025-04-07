package com.hbd.book_be.domain

import jakarta.persistence.*
import java.io.Serializable

data class TagContentsId(
    var tag: Tag, // 필드명 변경
    var contents: Contents // 필드명 변경
) : Serializable

@Entity
@Table(name = "tagcontents")
@IdClass(TagContentsId::class)
class TagContents(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false, referencedColumnName = "id")
    var tag: Tag,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contents_id", nullable = false, referencedColumnName = "id")
    var contents: Contents
)
