package com.hbd.book_be.domain.core


import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PostLoad
import jakarta.persistence.PrePersist
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.proxy.HibernateProxy
import org.hibernate.type.SqlTypes
import org.springframework.data.domain.Persistable
import kotlin.jvm.Transient

@MappedSuperclass
abstract class AutoIdEntity internal constructor() : BaseTimeEntity(), Persistable<Long> {

    @Id
    @JdbcTypeCode(SqlTypes.BIGINT)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    var id: Long = 0
        protected set

    final override fun getId(): Long? = if (id == 0L) null else id

    @Transient
    private var _isNew: Boolean = true

    final override fun isNew(): Boolean = _isNew

    @PrePersist
    @PostLoad
    private fun markNotNew() {
        _isNew = false
    }

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass =
            if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass
        val thisEffectiveClass =
            if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        other as AutoIdEntity

        return id == other.id
    }

    final override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()
}