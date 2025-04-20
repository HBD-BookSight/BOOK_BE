package com.hbd.book_be.service

import com.hbd.book_be.domain.Book
import com.hbd.book_be.domain.Publisher
import com.hbd.book_be.domain.Tag
import com.hbd.book_be.dto.PublisherDto
import com.hbd.book_be.dto.request.PublisherCreateRequest
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.repository.PublisherRepository
import com.hbd.book_be.repository.TagRepository
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
    private val tagRepository: TagRepository
) {
    private val log = LoggerFactory.getLogger(PublisherService::class.java)

    @Transactional(readOnly = true)
    fun getPublishers(
        page: Int = 0,
        limit: Int = 10,
        orderBy: String = "name",
        direction: String = "asc"
    ): Page<PublisherDto> {
        val sort = Sort.by(Sort.Direction.fromString(direction), orderBy)
        val pageable = PageRequest.of(page, limit, sort)

        return publisherRepository.findAllActive(pageable).map { PublisherDto.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    fun getPublisherDetail(id: Long): PublisherDto.Detail {
        val book = publisherRepository.findById(id).getOrNull()
        if (book == null || book.deletedAt != null) {
            throw NotFoundException("Not found Book(isbn: $id)")
        }

        return PublisherDto.Detail.fromEntity(book)
    }

    @Transactional
    fun createPublisher(publisherCreateRequest: PublisherCreateRequest): PublisherDto.Detail? {
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
                isOfficial = true,
            )
        }

        // Add new tags
        tagList.forEach { tag -> publisher.addTag(tag) }

        // Associate unique books with the publisher
        bookSet.forEach { book ->
            book.publisher = publisher
        }
        publisher.bookList = bookSet.toMutableList()

        return PublisherDto.Detail.fromEntity(publisher)
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