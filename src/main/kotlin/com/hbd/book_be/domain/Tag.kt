package com.hbd.book_be.domain

import com.hbd.book_be.domain.core.AutoIdEntity
import jakarta.persistence.*


@Entity
@Table(name = "tag")
class Tag internal constructor(
    builder: TagBuilder,
) : AutoIdEntity() {

    @Column(nullable = false, unique = true)
    var name: String = requireNotNull(builder.name) { "태그 이름은 필수입니다." }
        protected set
}

class TagBuilder internal constructor() {
    var name: String? = null

    internal fun build(): Tag {
        require(name?.isNotBlank() == true) { "태그 이름은 필수입니다." }
        require(name?.length ?: 0 <= 50) { "태그 이름은 50자 이내여야 합니다." }
        return Tag(this)
    }
}
