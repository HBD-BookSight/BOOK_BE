package com.hbd.book_be.event.publisher

interface EventPublisher {
    fun publishEvent(event: Any)
}