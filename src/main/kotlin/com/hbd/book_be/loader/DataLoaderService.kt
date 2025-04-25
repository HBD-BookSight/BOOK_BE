package com.hbd.book_be.loader

import com.hbd.book_be.domain.Book
import com.hbd.book_be.dto.request.BookCreateRequest
import com.hbd.book_be.loader.helper.BookJdbcHelper
import com.hbd.book_be.loader.helper.BookJpaHelper
import com.hbd.book_be.repository.AuthorRepository
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.repository.PublisherRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DataLoaderService(
    private val bookRepository: BookRepository,
    private val publisherRepository: PublisherRepository,
    private val authorRepository: AuthorRepository,
    jdbcTemplate: JdbcTemplate, // 👉 val 제거 (외부 노출 필요 없음)
) {
    private val jpaHelper = BookJpaHelper(publisherRepository, authorRepository)
    private val jdbcHelper = BookJdbcHelper(jdbcTemplate) // 👉 lookupHelper 제거

    @Transactional
    fun saveBooksWithJpa(requests: List<BookCreateRequest>) {
        val books = requests.mapNotNull { req ->
            runCatching {
                jpaHelper.createBookEntity(req)
            }.onFailure {
                println("❌ Book 생성 실패: ${req.title} (${it.message})")
            }.getOrNull()
        }

        runCatching {
            bookRepository.saveAll(books)
            println("✅ JPA 저장 완료 (${books.size}권)")
        }.onFailure {
            println("❌ JPA 저장 실패: ${it.message}")
            it.printStackTrace()
        }
    }

    @Transactional
    fun saveBooksWithJdbc(requests: List<BookCreateRequest>) {
        val books = mutableListOf<Book>()
        val bookAuthorPairs = mutableListOf<Pair<Int, Long>>()

        val publisherCache = mutableMapOf<String, Long>()
        val authorCache = mutableMapOf<String, Long>()

        requests.forEachIndexed { index, req ->
            runCatching {
                val publisherId = jdbcHelper.getOrInsertPublisherId(req.publisherName, publisherCache)
                val book = jdbcHelper.createBookStub(req, publisherId)
                books.add(book)

                req.authorNameList.forEach { name ->
                    val authorId = jdbcHelper.getOrInsertAuthorId(name, authorCache)
                    bookAuthorPairs.add(index to authorId)
                }
            }.onFailure {
                println("❌ Book 변환 실패: ${req.title} (${it.message})")
            }
        }

        jdbcHelper.insertBooksBatch(books)
        jdbcHelper.insertBookAuthorsBatch(bookAuthorPairs, books)
        println("✅ JDBC 저장 완료 (${books.size}권)")
    }
}
