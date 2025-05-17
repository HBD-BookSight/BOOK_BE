package com.hbd.book_be.aop

import com.hbd.book_be.dto.request.BookSearchRequest
import com.hbd.book_be.dto.response.PageResponse
import com.hbd.book_be.event.model.BookSearchEvent
import com.hbd.book_be.event.publisher.EventPublisher
import com.hbd.book_be.util.RequestContext
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Aspect
@Component
class BookSearchAspect(
    private val eventPublisher: EventPublisher
) {
    private val log = LoggerFactory.getLogger(BookSearchAspect::class.java)

    @Around("@annotation(LogBookSearch)")
    fun logBookSearch(joinPoint: ProceedingJoinPoint): Any? {
        val request = joinPoint.args[0] as BookSearchRequest

        val startTime = System.currentTimeMillis()
        try {
            val response = joinPoint.proceed()
            val durationMs = System.currentTimeMillis() - startTime

            if (response == null) {
                publishBookSearchEvent(
                    keyword = request.keyword,
                    totalCount = null,
                    status = "failed",
                    errorMessage = "response is null",
                    durationMs = durationMs
                )
            } else if (response !is ResponseEntity<*>) {
                publishBookSearchEvent(
                    keyword = request.keyword,
                    totalCount = null,
                    status = "failed",
                    errorMessage = "response type should be ResponseEntity, but this type is ${response::class.java}",
                    durationMs = durationMs
                )
            } else if (response.statusCode != HttpStatus.OK) {
                publishBookSearchEvent(
                    keyword = request.keyword,
                    totalCount = null,
                    status = "failed",
                    errorMessage = "response status code is ${response.statusCode}. body: ${response.body.toString()}",
                    durationMs = durationMs
                )
            } else {
                val pageResponse = response.body as PageResponse<*>
                publishBookSearchEvent(
                    keyword = request.keyword,
                    totalCount = pageResponse.totalCount,
                    status = "success",
                    durationMs = durationMs
                )
            }

            return response
        } catch (e: Exception) {
            val durationMs = System.currentTimeMillis() - startTime
            publishBookSearchEvent(
                keyword = request.keyword,
                totalCount = null,
                status = "failed",
                durationMs = durationMs,
                errorMessage = e.message
            )
            throw e
        }
    }

    private fun publishBookSearchEvent(
        keyword: String?,
        totalCount: Long?,
        status: String,
        durationMs: Long,
        errorMessage: String? = null,
    ) {
        try {
            val bookSearchEvent = BookSearchEvent(
                requestId = RequestContext.getRequestId(),
                keyword = keyword,
                totalCount = totalCount,
                status = status,
                errorMessage = errorMessage,
                durationMs = durationMs
            )
            eventPublisher.publishEvent(bookSearchEvent)
        } catch (e: Exception) {
            log.error("Failed to publish book search event", e)
        }
    }

}