package com.hbd.book_be.domain

import com.hbd.book_be.domain.core.BaseTimeEntity
import jakarta.persistence.*


@Entity
@Table(
    name = "contact",
    indexes = [
        Index(name = "idx_contact_email", columnList = "email")
    ]
)
class Contact internal constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "name")
    var name: String?,

    @Column(nullable = false)
    var email: String,

    @Column(nullable = false, length = 3000)
    var message: String
) : BaseTimeEntity()