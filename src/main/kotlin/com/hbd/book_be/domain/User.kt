package com.hbd.book_be.domain

import com.hbd.book_be.domain.core.BaseTimeEntity
import com.hbd.book_be.enums.UserRole
import jakarta.persistence.*

@Entity
@Table(name = "users",
    indexes = [
        Index(name = "idx_user_name", columnList = "name"),
        Index(name = "idx_user_name_role", columnList = "name, role"),
    ]
)
class User (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)  // DB에 ENUM 값으로 저장되게 하려면 이 애노테이션 추가
    @Column(nullable = false)
    var role: UserRole,

    ) : BaseTimeEntity()

