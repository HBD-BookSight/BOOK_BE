package com.hbd.book_be.domain

import com.hbd.book_be.domain.core.AutoIdEntity
import jakarta.persistence.*

@Entity
@Table(name = "author")
class Author internal constructor(
    builder: AuthorBuilder,
) : AutoIdEntity(){

    @Column(name = "koNm")
    var koNm: String? = builder.koNm
        protected set
    @Column(name = "enNm")
    var enNm: String? = builder.enNm
        protected set

    companion object {
        fun builder(): UserBuilder = UserBuilder()
    }
}

class AuthorBuilder internal constructor() {
    var koNm: String? = null
    var enNm: String? = null

    internal fun build(): Author {
        koNm?.let {
            require(it.contains(Regex("[가-힣]"))) {
                "한글 이름에는 한글이 최소 1글자 이상 포함되어야 합니다."
            }
        }
        enNm?.let {
            require(it.matches(Regex("^[a-zA-Z\\s]*$"))) {
                "영문 이름은 알파벳과 공백만 포함할 수 있습니다."
            }
        }

        return Author(this)
    }
}