package com.hbd.book_be.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "discovery_contents",
    indexes = [
        Index(name = "idx_discovery_contents_created_date", columnList = "created_at")
    ]
)
class DiscoveryContents(
    @Id
    var contentId: Long,

    @MapsId(value="contentId")
    @OneToOne(targetEntity = Contents::class, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "contents_id", nullable = false)
    var contents: Contents,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
