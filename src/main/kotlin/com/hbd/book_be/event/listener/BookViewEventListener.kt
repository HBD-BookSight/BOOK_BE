package com.hbd.book_be.event.listener

import com.hbd.book_be.domain.BookViewLog
import com.hbd.book_be.event.model.BookViewEvent
import com.hbd.book_be.repository.BookViewLogRepository
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class BookViewEventListener(
    private val bookViewLogRepository: BookViewLogRepository
) {
    private val log = LoggerFactory.getLogger(BookViewEventListener::class.java)

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleBookViewEvent(event: BookViewEvent) {
        log.info("handle bookViewEvent: $event")
        val bookViewLog = BookViewLog(
            requestId = event.requestId,
            isbn = event.isbn,
            title = event.title,
            status = event.status,
            errorMessage = event.errorMessage,
            durationMs = event.durationMs,
            userId = event.userId,
            sourceKeyword = event.sourceKeyword,
            sourcePath = event.sourcePath,
            viewDateTime = event.viewDateTime,
        )

        bookViewLogRepository.save(bookViewLog)
    }

}