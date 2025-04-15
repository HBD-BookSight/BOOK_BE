package com.hbd.book_be.service

import com.hbd.book_be.dto.PublisherDto
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.repository.PublisherRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class PublisherService(
    @Autowired
    private val publisherRepository: PublisherRepository
) {

    fun getPublishers(): List<PublisherDto> {
        return publisherRepository.findAll().map { PublisherDto.fromEntity(it) }
    }

    fun getPublisherDetail(id: Long): PublisherDto {
        val book = publisherRepository.findById(id).getOrNull()
        if (book == null || book.deletedAt != null) {
            throw NotFoundException("Not found Book(isbn: $id)")
        }

        return PublisherDto.fromEntity(book)
    }
}