package com.hbd.book_be.service

import com.hbd.book_be.dto.BookDetailedDto
import com.hbd.book_be.dto.BookDto
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.repository.BookRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class BookService(
    @Autowired
    private val bookRepository: BookRepository
) {
    fun getBooks(page: Int, limit: Int, orderBy: String, direction: String): Page<BookDto> {
        val sortDirection = Sort.Direction.fromString(direction)
        val sort = Sort.by(sortDirection, orderBy)
        val pageRequest = PageRequest.of(page, limit, sort)

        val bookPage = bookRepository.findAllNonDeletedBook(pageRequest)
        return bookPage.map { BookDto.fromEntity(it) }
    }

    fun getBookDetail(isbn: String): BookDetailedDto {
        val book = bookRepository.findById(isbn).getOrNull()
        if (book == null || book.deletedAt != null) {
            throw NotFoundException("Not found Book(isbn: $isbn)")
        }

        return BookDetailedDto.fromEntity(book)
    }
}