package com.hbd.book_be.loader.helper

import com.hbd.book_be.domain.Author
import com.hbd.book_be.domain.Book
import com.hbd.book_be.domain.Publisher
import com.hbd.book_be.dto.request.BookCreateRequest
import com.hbd.book_be.repository.AuthorRepository
import com.hbd.book_be.repository.PublisherRepository

class BookJpaHelper(
    private val publisherRepository: PublisherRepository,
    private val authorRepository: AuthorRepository
) {
    private val publisherCache = mutableMapOf<String, Publisher>()
    private val authorCache = mutableMapOf<String, Author>()

    fun createBookEntity(req: BookCreateRequest): Book {
        val publisherName = req.publisherName?.takeIf { it.isNotBlank() } ?: "알 수 없음"
        val publisher = publisherCache.getOrPut(publisherName) {
            publisherRepository.findByName(publisherName)
                ?: publisherRepository.save(
                    Publisher(
                        name = publisherName,
                        engName = null,
                        logo = null,
                        description = null,
                        isOfficial = false
                    )
                )
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

        return book
    }
}
