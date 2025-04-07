package com.hbd.book_be.domain

import com.hbd.book_be.domain.core.AutoIdEntity
import jakarta.persistence.*


@Entity
@Table(name = "contact")
class Contact internal constructor(
    builder: ContactBuilder,
) : AutoIdEntity() {

    @Column(nullable = false)
    var email: String = requireNotNull(builder.email) { "이메일은 필수입니다." }
        protected set

    @Column(nullable = false, length = 3000)
    var message: String = requireNotNull(builder.message) { "메시지는 필수입니다." }
        protected set

}

class ContactBuilder internal constructor() {
    var email: String? = null
    var message: String? = null

    internal fun build(): Contact {
        require(email?.contains("@") == true) {
            "이메일 형식이 올바르지 않습니다."
        }
        require(message?.isNotBlank() == true) {
            "메시지는 비워둘 수 없습니다."
        }
        return Contact(this)
    }
}
