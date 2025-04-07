package com.hbd.book_be.domain

import com.hbd.book_be.domain.core.AutoIdEntity
import jakarta.persistence.*

@Entity
@Table(name = "user")
class User internal constructor(
    builder: UserBuilder,
) : AutoIdEntity() {

    @Column(nullable = false)
    var password: String? = builder.password
        protected set

    @Column(nullable = false)
    var name: String? = builder.name
        protected set

    @Enumerated(EnumType.STRING)  // DB에 ENUM 값으로 저장되게 하려면 이 애노테이션 추가
    @Column(nullable = false)
    var role: UserRole? = builder.role
        protected set

}

class UserBuilder internal constructor() {
    var password: String? = null
    var name: String? = null
    var role: UserRole? = null

    internal fun build(): User {
        require(password?.length ?: 0 >= 6) { "비밀번호는 최소 6자 이상이어야 합니다." }
        require(name?.isNotBlank() == true) { "이름은 필수입니다." }
        require(role != null) { "역할은 필수입니다." }

        return User(this)
    }
}

fun User(
    builder: UserBuilder = UserBuilder(),
    buildToAction: UserBuilder.() -> Unit = {},
): User = builder.apply(buildToAction).build()
