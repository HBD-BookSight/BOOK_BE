package com.hbd.book_be.repository

import com.hbd.book_be.domain.Event
import org.springframework.data.jpa.repository.JpaRepository

interface EventRepository : JpaRepository<Event, Long>, EventRepositoryCustom