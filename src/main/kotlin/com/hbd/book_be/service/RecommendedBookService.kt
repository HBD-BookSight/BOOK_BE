package com.hbd.book_be.service

import com.hbd.book_be.domain.RecommendedBook
import com.hbd.book_be.dto.RecommendedBookDto
import com.hbd.book_be.dto.request.RecommendedBookCreateRequest
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.repository.RecommendedBookRepository
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Service
class RecommendedBookService(
    @Autowired
    private val recommendedBookRepository: RecommendedBookRepository,

    @Autowired
    private val bookRepository: BookRepository
) {

    @Transactional
    fun createRecommendedBook(request: RecommendedBookCreateRequest): RecommendedBookDto {
        // 책이 존재하는지 확인
        val book = bookRepository.findById(request.isbn).getOrNull()
            ?: throw NotFoundException("Book not found with ISBN: ${request.isbn}")

        // 이미 추천 도서로 등록되어 있는지 확인
        if (recommendedBookRepository.existsById(request.isbn)) {
            throw IllegalArgumentException("Book with ISBN ${request.isbn} is already recommended")
        }

        val recommendedBook = RecommendedBook(
            isbn = request.isbn,
            book = book,
            recommendedDate = LocalDate.now(),
            createdAt = LocalDateTime.now()
        )

        val savedRecommendedBook = recommendedBookRepository.save(recommendedBook)
        return RecommendedBookDto.fromEntity(savedRecommendedBook)
    }

    @Transactional
    fun deleteRecommendedBook(isbn: String) {
        if (!recommendedBookRepository.existsById(isbn)) {
            throw NotFoundException("Recommended book not found with ISBN: $isbn")
        }
        recommendedBookRepository.deleteById(isbn)
    }
}
