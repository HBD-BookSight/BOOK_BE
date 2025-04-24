package com.hbd.book_be.loader

import com.hbd.book_be.domain.Author
import com.hbd.book_be.domain.Book
import com.hbd.book_be.domain.Publisher
import com.hbd.book_be.dto.request.BookCreateRequest
import com.hbd.book_be.repository.AuthorRepository
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.repository.PublisherRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DataLoaderService(
    private val bookRepository: BookRepository,
    private val publisherRepository: PublisherRepository,
    private val authorRepository: AuthorRepository
) {

    @Transactional
    fun saveBooksWithJpa(requests: List<BookCreateRequest>) {
        val publisherCache = mutableMapOf<String, Publisher>()
        val authorCache = mutableMapOf<String, Author>()

        val books = requests.mapNotNull { req ->
            try {
                val name = req.publisherName?.takeIf { it.isNotBlank() } ?: "알 수 없음"

                val publisher = publisherCache[name] ?: run {
                    val found = publisherRepository.findByName(name)
                    if (found != null) {
                        publisherCache[name] = found
                        found
                    } else {
                        val saved = publisherRepository.save(
                            Publisher(name = name, engName = null, logo = null, description = null)
                        )
                        publisherCache[name] = saved
                        saved
                    }
                }

                val book = Book(
                    isbn = req.isbn,
                    title = req.title,
                    summary = req.summary,
                    publishedDate = req.publishedDate,
                    titleImage = req.titleImage,
                    price = req.price,
                    publisher = publisher,
                    detailUrl = req.detailUrl,
                    translator = req.translator
                )

                req.authorNameList.forEach { name ->
                    val author = authorCache.getOrPut(name) {
                        authorRepository.findTopByNameAndDeletedAtIsNull(name)
                            ?: authorRepository.save(Author(name = name, description = null, profile = null))
                    }
                    book.addAuthor(author)
                }

                book
            } catch (e: Exception) {
                println("❌ Book 생성 실패: ${req.title} (${e.message})")
                null
            }
        }

        try {
            bookRepository.saveAll(books)
            println("✅ 전체 ${books.size}권 저장 완료")
        } catch (e: Exception) {
            println("❌ 전체 저장 실패: ${e.message}")
            e.printStackTrace()
        }
    }
}
