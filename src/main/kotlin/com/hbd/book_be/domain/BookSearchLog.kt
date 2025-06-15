package com.hbd.book_be.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
class BookSearchLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "request_id", nullable = false)
    val requestId: String,

    @Column(name = "keyword", nullable = true)
    val keyword: String?,

    @Column(name = "total_count", nullable = true)
    val totalCount: Long?,

    @Column(name = "status", nullable = false)
    val status: String,

    @Column(name = "duration_ms", nullable = false)
    val durationMs: Long,

    @JdbcTypeCode(SqlTypes.NCLOB)
    @Column(name = "error_message", nullable = true) // OCI DB doesn't support 'TEXT'
    val errorMessage: String?,

    @Column(name = "search_date_time", nullable = false)
    val searchDateTime: LocalDateTime,
)