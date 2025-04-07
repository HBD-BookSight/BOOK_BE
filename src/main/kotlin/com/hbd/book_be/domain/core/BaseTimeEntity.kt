package com.hbd.book_be.domain.core

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeEntity (
    createdAt: LocalDateTime =   LocalDateTime.now(),
    updatedAt: LocalDateTime =  LocalDateTime.now()
    ){

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = createdAt
        protected set

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = updatedAt
        protected set

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
        protected set

    fun markDeleted() {
        this.deletedAt = LocalDateTime.now()
    }
}
