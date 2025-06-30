package com.hbd.book_be.service

import com.hbd.book_be.domain.Book
import com.hbd.book_be.domain.Publisher
import com.hbd.book_be.domain.Tag
import com.hbd.book_be.dto.PublisherDto
import com.hbd.book_be.dto.request.PublisherCreateRequest
import com.hbd.book_be.dto.request.PublisherUpdateRequest
import com.hbd.book_be.dto.request.enums.PublisherSortBy
import com.hbd.book_be.dto.request.enums.SortDirection
import com.hbd.book_be.enums.UserRole
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.repository.PublisherRepository
import com.hbd.book_be.repository.TagRepository
import com.hbd.book_be.util.AuthUtils
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class PublisherService(
    private val publisherRepository: PublisherRepository,
    private val bookRepository: BookRepository,
    private val tagRepository: TagRepository,
    private val authUtils: AuthUtils
) {
    private val log = LoggerFactory.getLogger(PublisherService::class.java)

    @Transactional(readOnly = true)
    fun getPublishers(
        page: Int = 0,
        limit: Int = 10,
        orderBy: PublisherSortBy,
        direction: SortDirection
    ): Page<PublisherDto> {
        val sort = Sort.by(Sort.Direction.fromString(direction.name), orderBy.value)
        val pageable = PageRequest.of(page, limit, sort)

        return publisherRepository.findAllActive(pageable).map { PublisherDto.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    fun getPublisherDetail(id: Long): PublisherDto.Detail {
        val publisher = publisherRepository.findById(id).getOrNull()
        if (publisher == null || publisher.deletedAt != null) {
            throw NotFoundException("Not found publisher(isbn: $id)")
        }

        return PublisherDto.Detail.fromEntity(publisher)
    }

    @Transactional
    fun createPublisher(publisherCreateRequest: PublisherCreateRequest): PublisherDto.Detail {
        val bookSet = mutableSetOf<Book>()
        val tagList = getOrCreateTagList(publisherCreateRequest.tagList)
        val bookList = bookRepository.findAllById(publisherCreateRequest.bookIsbnList)
        bookSet.addAll(bookList)

        // Get or create publisher
        var publisher = publisherRepository.findByName(publisherCreateRequest.name)
        if (publisher != null) {
            log.info("Updating existing publisher with name ${publisher.name}(id=${publisher.id})")
            // Update existing publisher
            publisher.apply {
                engName = publisherCreateRequest.engName
                description = publisherCreateRequest.description
                logo = publisherCreateRequest.logo
                urls = publisherCreateRequest.urls.toMutableList()
                isOfficial = true
                // Clear existing relationships
                bookSet.addAll(this.bookList)
                this.bookList.clear()
                clearTags()
                publisherTitleBooksList.clear()
            }
        } else {
            // Create new publisher
            publisher = Publisher(
                name = publisherCreateRequest.name,
                engName = publisherCreateRequest.engName,
                description = publisherCreateRequest.description,
                logo = publisherCreateRequest.logo,
                urls = publisherCreateRequest.urls.toMutableList(),
                isOfficial = true
            )
        }

        // Add new tags
        tagList.forEach { tag -> publisher.addTag(tag) }

        // Associate unique books with the publisher
        bookSet.forEach { book ->
            book.publisher = publisher
        }
        publisher.bookList = bookSet.toMutableList()

        val savedPublisher = publisherRepository.save(publisher)

        return PublisherDto.Detail.fromEntity(savedPublisher)
    }

    @Transactional
    fun updatePublisher(id: Long, publisherUpdateRequest: PublisherUpdateRequest): PublisherDto.Detail {
        val publisher = publisherRepository.findById(id).getOrNull()
            ?: throw NotFoundException("Not found publisher(id: $id)")

        // ADMIN 권한 확인
        if (!authUtils.isCurrentUserAdmin()) {
            throw IllegalAccessException("수정 권한이 없습니다. 관리자만 수정할 수 있습니다.")
        }

        // 필드 업데이트 (null이 아닌 값만)
        publisherUpdateRequest.name?.let { publisher.name = it }
        publisherUpdateRequest.engName?.let { publisher.engName = it }
        publisherUpdateRequest.logo?.let { publisher.logo = it }
        publisherUpdateRequest.description?.let { publisher.description = it }
        publisherUpdateRequest.urls?.let { publisher.urls = it.toMutableList() }

        // 태그 업데이트
        publisherUpdateRequest.tagList?.let { tagNames ->
            publisher.clearTags()
            val tagList = getOrCreateTagList(tagNames)
            tagList.forEach { tag -> publisher.addTag(tag) }
        }

        // 책 목록 업데이트
        publisherUpdateRequest.bookIsbnList?.let { bookIsbnList ->
            val bookList = bookRepository.findAllById(bookIsbnList)
            publisher.bookList.clear()
            bookList.forEach { book ->
                book.publisher = publisher
                publisher.bookList.add(book)
            }
        }

        val savedPublisher = publisherRepository.save(publisher)
        return PublisherDto.Detail.fromEntity(savedPublisher)
    }

    @Transactional
    fun deletePublisher(id: Long) {
        val publisher = publisherRepository.findById(id).getOrNull()
            ?: throw NotFoundException("Not found publisher(id: $id)")

        // ADMIN 권한 확인
        if (!authUtils.isCurrentUserAdmin()) {
            throw IllegalAccessException("삭제 권한이 없습니다. 관리자만 삭제할 수 있습니다.")
        }

        // Soft delete
        publisher.deletedAt = java.time.LocalDateTime.now()
        publisherRepository.save(publisher)
    }

    private fun getOrCreateTagList(tagList: List<String>): List<Tag> {
        val newTagList = mutableListOf<Tag>()
        for (tagName in tagList) {
            var tag = tagRepository.findByName(tagName)
            if (tag == null) {
                tag = tagRepository.save(Tag(name = tagName))
            }
            newTagList.add(tag)
        }

        return newTagList
    }
}
