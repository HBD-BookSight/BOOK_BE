package com.hbd.book_be.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "book_view_log",
    indexes = [
        Index(name = "idx_view_datetime", columnList = "view_date_time")
    ]
)
class BookViewLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "isbn", nullable = false)
    val isbn: String,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name="status")
    val status: String,

    @Column(name="error_message")
    val errorMessage: String?,

    @Column(name="duration_ms")
    val durationMs: Long?,

    @Column(name = "user_id")
    val userId: Long?,

    @Column(name = "source_keyword")
    val sourceKeyword: String?,

    @Column(name = "source_path")
    val sourcePath: String?,

    @Column(name = "view_date_time")
    val viewDateTime: LocalDateTime
)