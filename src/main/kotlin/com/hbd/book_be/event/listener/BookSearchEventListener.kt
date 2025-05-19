package com.hbd.book_be.event.listener

import com.hbd.book_be.domain.BookSearchLog
import com.hbd.book_be.event.model.BookSearchEvent
import com.hbd.book_be.repository.BookSearchLogRepository
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class BookSearchEventListener(
    private val bookSearchLogRepository: BookSearchLogRepository
) {
    private val log = LoggerFactory.getLogger(BookSearchEventListener::class.java)

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleBookSearchEvent(event: BookSearchEvent) {
        log.info("handle bookSearchEvent: $event")
        val bookSearchLog = BookSearchLog(
            requestId = event.requestId,
            keyword = event.keyword,
            totalCount = event.totalCount,
            status = event.status,
            durationMs = event.durationMs,
            errorMessage = event.errorMessage,
            searchDateTime = event.searchDateTime
        )

        bookSearchLogRepository.save(bookSearchLog)
    }

}