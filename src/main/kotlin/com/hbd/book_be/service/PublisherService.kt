package com.hbd.book_be.service

import com.hbd.book_be.dto.PublisherDetaildDto
import com.hbd.book_be.dto.PublisherDto
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.repository.PublisherRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class PublisherService(
    private val publisherRepository: PublisherRepository,
    private val bookRepository: BookRepository
) {

    @Transactional(readOnly = true)
    fun getPublishers(): List<PublisherDto> {
        return publisherRepository.findAll().map { PublisherDto.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    fun getPublisherDetail(id: Long): PublisherDetaildDto {
        val book = publisherRepository.findById(id).getOrNull()
        if (book == null || book.deletedAt != null) {
            throw NotFoundException("Not found Book(isbn: $id)")
        }

        return PublisherDetaildDto.fromEntity(book)
    }
}