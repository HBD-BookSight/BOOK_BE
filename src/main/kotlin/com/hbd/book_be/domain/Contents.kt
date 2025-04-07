package com.hbd.book_be.domain

import com.hbd.book_be.domain.core.AutoIdEntity
import jakarta.persistence.*

@Entity
@Table(name = "contents")
class Contents internal constructor(
    builder: ContentsBuilder
) : AutoIdEntity() {
    // enum 변경 필요
    @Column(name = "type" , nullable = false )
    var type: String? = builder.type
        protected set

    @Column(name = "url", nullable = false)
    var url: String? = builder.url
        protected set

    @Column(name = "image")
    var image: String? = builder.image
        protected set

    @Column(name = "description")
    var description: String? = builder.description
        protected set

    @Column(name = "memo")
    var memo: String? = builder.memo
        protected set

    @Column(name = "created")
    var created: Long? = builder.created
        protected set

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contents", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val bookContentsList: MutableList<BookContents> = mutableListOf()
    val bookContents: List<BookContents> get() = bookContentsList.toList()

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contents", cascade = [CascadeType.ALL], orphanRemoval = true)
    val tagContentsList: MutableList<TagContents> = mutableListOf()
    val tagContents: List<TagContents> get() = tagContentsList.toList()

    @ElementCollection
    @CollectionTable(name = "discovery_contents", joinColumns = [JoinColumn(name = "contents_id")])
    private val discoveriesList: MutableList<DiscoveryContent> = mutableListOf()
    val discoveries: List<DiscoveryContent> get() = discoveriesList.toList()

}

class ContentsBuilder internal constructor() {
    var id: Long? = null
    var type: String? = null
    var url: String? = null
    var image: String? = null
    var description: String? = null
    var memo: String? = null
    var created: Long? = null

    fun build(): Contents {
        // 필요한 필드만 검증 (원하시면 여기에 더 추가 가능)
        require(!type.isNullOrBlank()) { "type은 필수입니다." }
        require(!url.isNullOrBlank()) { "url은 필수입니다." }

        return Contents(this)
    }
}
