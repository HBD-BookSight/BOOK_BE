package com.hbd.book_be.domain

import com.hbd.book_be.domain.core.AutoIdEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "event")
class Event internal constructor(
    builder: EventBuilder
) : AutoIdEntity() {
    @Column(name = "title")
    var title: String? = builder.title
        protected set

    @Column(name = "owner")
    var owner: String? = builder.owner
        protected set

    @Column(name = "url")
    var url: String? = builder.url
        protected set

    @Column(name = "sender_email")
    var senderEmail: String? = builder.senderEmail
        protected set

    @Column(name = "sender_message")
    var senderMessage: String? = builder.senderMessage
        protected set

    @Column(name = "location")
    var location: String? = builder.location
        protected set

    @Column(name = "status")
    var status: Boolean? = builder.status
        protected set

    @Column(name = "start_date")
    var startDate: LocalDateTime? = builder.startDate
        protected set

    @Column(name = "end_date")
    var endDate: LocalDateTime? = builder.endDate
        protected set

    @Column(name = "event_type")
    var eventType: String? = builder.eventType
        protected set

    @Column(name = "event_flag")
    var eventFlag: String? = builder.eventFlag
        protected set

    @Column(name = "memo")
    var memo: String? = builder.memo
        protected set

    @OneToMany(fetch = FetchType.LAZY,  mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true)
    val tagEventsList: MutableList<TagEvent> = mutableListOf()
    val tagEvents : List<TagEvent> get() = tagEventsList.toList()

    @OneToMany(fetch = FetchType.LAZY,  mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true)
    val bookEventList: MutableList<BookEvent> = mutableListOf()
    val bookEvent : List<BookEvent> get() = bookEventList.toList()

}

class EventBuilder internal constructor() {
    var title: String? = null
    var owner: String? = null
    var url: String? = null
    var senderEmail: String? = null
    var senderMessage: String? = null
    var location: String? = null
    var status: Boolean? = null
    var startDate: LocalDateTime? = null
    var endDate: LocalDateTime? = null
    var eventType: String? = null
    var eventFlag: String? = null
    var memo: String? = null

    fun build(): Event {
        // 유효성 검사 예시 (필요시 추가)
        require(!title.isNullOrBlank()) { "title은 필수입니다." }
        require(startDate != null) { "startDate는 필수입니다." }
        require(endDate != null) { "endDate는 필수입니다." }

        return Event(this)
    }
    }