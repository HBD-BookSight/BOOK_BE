package com.hbd.book_be.service

import com.hbd.book_be.domain.Event
import com.hbd.book_be.domain.Tag
import com.hbd.book_be.dto.BookDto
import com.hbd.book_be.dto.EventDto
import com.hbd.book_be.dto.request.EventCreateRequest
import com.hbd.book_be.dto.request.EventSearchRequest
import com.hbd.book_be.dto.request.EventUpdateRequest
import com.hbd.book_be.exception.ErrorCodes
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.exception.ValidationException
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.repository.EventRepository
import com.hbd.book_be.repository.TagRepository
import com.hbd.book_be.repository.UserRepository
import com.hbd.book_be.util.AuthUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class EventService(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val bookRepository: BookRepository,
    private val tagRepository: TagRepository,
    private val authUtils: AuthUtils
) {
    @Transactional(readOnly = true)
    fun getEvent(eventId: Long): EventDto {
        val event = eventRepository.findById(eventId).getOrNull()
        if (event == null) {
            throw NotFoundException("Not found Event(eventId: $eventId)")
        }

        if (!event.isPosting) {
            // TODO(moonkyung): check user authentication.
            throw ValidationException(
                message = "Access Denied to event(${event.id})",
                errorCode = ErrorCodes.EVENT_ACCESS_DENIED,
                status = HttpStatus.FORBIDDEN
            )
        }

        return EventDto.fromEntity(event)
    }


    @Transactional(readOnly = true)
    fun getEvents(page: Int, limit: Int, searchRequest: EventSearchRequest): Page<EventDto> {
        val pageRequest = PageRequest.of(page, limit, Sort.by(Sort.Direction.ASC, "startDate"))
        val eventPage = eventRepository.findAllActiveWithConditions(searchRequest, pageRequest)
        return eventPage.map { EventDto.fromEntity(it) }
    }


    @Transactional(readOnly = true)
    fun getEventBooks(eventId: Long): List<BookDto> {
        val event = eventRepository.findById(eventId).getOrNull()
        if (event == null) {
            throw NotFoundException("Not found Event(eventId: $eventId)")
        }

        if (!event.isPosting) {
            // TODO(moonkyung): check user authentication.
            throw ValidationException(
                message = "Access Denied to event(${event.id})",
                errorCode = ErrorCodes.EVENT_ACCESS_DENIED,
                status = HttpStatus.FORBIDDEN
            )
        }

        return event.bookEventList.map { BookDto.fromEntity(it.book) }
    }

    @Transactional
    fun createEvent(eventCreateRequest: EventCreateRequest): EventDto {
        val user = userRepository.findById(eventCreateRequest.userId).getOrNull()
        if (user == null) {
            throw NotFoundException("Not found User(${eventCreateRequest.userId})")
        }

        val tagList = getOrCreateTagList(eventCreateRequest)
        val bookList = bookRepository.findAllById(eventCreateRequest.bookIsbnList)

        var event = Event(
            title = eventCreateRequest.title,
            host = eventCreateRequest.host,
            urls = eventCreateRequest.urls.toMutableList(),
            startDate = eventCreateRequest.startDate,
            endDate = eventCreateRequest.endDate,
            location = eventCreateRequest.location,
            eventType = eventCreateRequest.eventType,
            eventFlag = eventCreateRequest.eventFlag,
            isPosting = eventCreateRequest.isPosting,
            bookTitle = eventCreateRequest.bookTitle,
            senderName = eventCreateRequest.senderName,
            senderEmail = eventCreateRequest.senderEmail,
            senderMessage = eventCreateRequest.senderMessage,
            memo = eventCreateRequest.memo,
            creator = user
        )

        tagList.forEach {
            event.addTag(it)
        }
        bookList.forEach {
            event.addBook(it)
        }

        event = eventRepository.save(event)
        return EventDto.fromEntity(event)
    }

    @Transactional
    fun updateEvent(id: Long, eventUpdateRequest: EventUpdateRequest): EventDto {
        val event = eventRepository.findById(id).getOrNull()
            ?: throw NotFoundException("Not found event(id: $id)")

        // 필드 업데이트 (null이 아닌 값만)
        eventUpdateRequest.title?.let { event.title = it }
        eventUpdateRequest.host?.let { event.host = it }
        eventUpdateRequest.urls?.let { event.urls = it.toMutableList() }
        eventUpdateRequest.startDate?.let { event.startDate = it }
        eventUpdateRequest.endDate?.let { event.endDate = it }
        eventUpdateRequest.location?.let { event.location = it }
        eventUpdateRequest.eventType?.let { event.eventType = it }
        eventUpdateRequest.eventFlag?.let { event.eventFlag = it }
        eventUpdateRequest.isPosting?.let { event.isPosting = it }
        eventUpdateRequest.bookTitle?.let { event.bookTitle = it }
        eventUpdateRequest.senderName?.let { event.senderName = it }
        eventUpdateRequest.senderEmail?.let { event.senderEmail = it }
        eventUpdateRequest.senderMessage?.let { event.senderMessage = it }
        eventUpdateRequest.memo?.let { event.memo = it }

        // 태그 업데이트
        eventUpdateRequest.tagList?.let { tagNames ->
            // 기존 태그 제거
            event.tagEventList.clear()
            val tagList = getOrCreateTagList(tagNames)
            tagList.forEach { tag -> event.addTag(tag) }
        }

        // 책 목록 업데이트
        eventUpdateRequest.bookIsbnList?.let { bookIsbnList ->
            val bookList = bookRepository.findAllById(bookIsbnList)
            event.bookEventList.clear()
            bookList.forEach { book -> event.addBook(book) }
        }

        val savedEvent = eventRepository.save(event)
        return EventDto.fromEntity(savedEvent)
    }

    @Transactional
    fun deleteEvent(id: Long) {
        val event = eventRepository.findById(id).getOrNull()
            ?: throw NotFoundException("Not found event(id: $id)")

        // Soft delete
        event.deletedAt = java.time.LocalDateTime.now()
        eventRepository.save(event)
    }

    private fun getOrCreateTagList(eventCreateRequest: EventCreateRequest): List<Tag> {
        val tagList = mutableListOf<Tag>()
        for (tagName in eventCreateRequest.tagList) {
            var tag = tagRepository.findByName(tagName)
            if (tag == null) {
                tag = tagRepository.save(Tag(name = tagName))
            }
            tagList.add(tag)
        }

        return tagList
    }

    private fun getOrCreateTagList(tagNames: List<String>): List<Tag> {
        val tagList = mutableListOf<Tag>()
        for (tagName in tagNames) {
            var tag = tagRepository.findByName(tagName)
            if (tag == null) {
                tag = tagRepository.save(Tag(name = tagName))
            }
            tagList.add(tag)
        }
        return tagList
    }

}