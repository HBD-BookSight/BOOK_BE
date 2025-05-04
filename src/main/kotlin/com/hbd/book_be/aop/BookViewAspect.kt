package com.hbd.book_be.aop

import com.hbd.book_be.dto.BookDto
import com.hbd.book_be.dto.request.BookDetailRequest
import com.hbd.book_be.event.model.BookViewEvent
import com.hbd.book_be.event.publisher.EventPublisher
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Aspect
@Component
class BookViewAspect(
    private val eventPublisher: EventPublisher
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Around("@annotation(LogBookView)")
    fun logBookView(joinPoint: ProceedingJoinPoint): Any? {
        val request = joinPoint.args[0] as BookDetailRequest

        val startTime = System.currentTimeMillis()
        try {
            val response = joinPoint.proceed()
            val durationMs = System.currentTimeMillis() - startTime
            if (response != null && response is ResponseEntity<*>) {
                publishBookViewEvent(request, response, durationMs)
            }


            return response
        } catch (e: Exception) {
            val durationMs = System.currentTimeMillis() - startTime
            publishFailedBookViewEvent(request, e, durationMs)
            throw e
        }
    }

    private fun publishBookViewEvent(
        request: BookDetailRequest,
        response: ResponseEntity<*>,
        durationMs: Long
    ) {
        try {
            val bookDetail = response.body
            if (bookDetail != null && bookDetail is BookDto.Detail) {
                eventPublisher.publishEvent(
                    BookViewEvent(
                        isbn = request.isbn,
                        title = bookDetail.title,
                        userId = null,
                        status = "success",
                        errorMessage = null,
                        durationMs = durationMs,
                        sourcePath = request.sourcePath,
                        sourceKeyword = request.sourceKeyword
                    )
                )
            }

        } catch (e: Exception) {
            log.error("Failed to publish book view event", e)
        }
    }

    private fun publishFailedBookViewEvent(request: BookDetailRequest, exception: Exception, durationMs: Long) {
        try {
            eventPublisher.publishEvent(
                BookViewEvent(
                    isbn = request.isbn,
                    title = "Unknown",
                    userId = null,
                    status = "failed",
                    errorMessage = exception.message,
                    durationMs = durationMs,
                    sourcePath = request.sourcePath,
                    sourceKeyword = request.sourceKeyword
                )
            )
        } catch (e: Exception) {
            log.error("Failed to publish failed book view event", e)
        }
    }
} 