package com.hbd.book_be.event.publisher

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) : EventPublisher {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun publishEvent(event: Any) {
        log.info("publish event: $event")
        applicationEventPublisher.publishEvent(event)
    }
}