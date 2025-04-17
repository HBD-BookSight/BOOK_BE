package com.hbd.book_be.repository

import com.hbd.book_be.domain.Event
import com.hbd.book_be.dto.request.EventSearchRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface EventRepositoryCustom {
    fun findEventsWithConditions(searchRequest: EventSearchRequest, pageable: Pageable): Page<Event>
}