package com.hbd.book_be.service

import com.hbd.book_be.domain.Author
import com.hbd.book_be.domain.Book
import com.hbd.book_be.domain.Publisher
import com.hbd.book_be.dto.BookDetailedDto
import com.hbd.book_be.dto.BookDto
import com.hbd.book_be.dto.request.BookAddRequest
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.repository.AuthorRepository
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.repository.PublisherRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class BookService(
    @Autowired
    private val bookRepository: BookRepository,

    @Autowired
    private val authorRepository: AuthorRepository,

    @Autowired
    private val publisherRepository: PublisherRepository,
) {
    @Transactional(readOnly = true)
    fun getBooks(page: Int, limit: Int, orderBy: String, direction: String): Page<BookDto> {
        val sortDirection = Sort.Direction.fromString(direction)
        val sort = Sort.by(sortDirection, orderBy)
        val pageRequest = PageRequest.of(page, limit, sort)

        val bookPage = bookRepository.findAllNonDeletedBook(pageRequest)
        return bookPage.map { BookDto.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    fun getBookDetail(isbn: String): BookDetailedDto {
        val book = bookRepository.findById(isbn).getOrNull()
        if (book == null || book.deletedAt != null) {
            throw NotFoundException("Not found Book(isbn: $isbn)")
        }

        return BookDetailedDto.fromEntity(book)
    }

    @Transactional
    fun addBook(bookAddRequest: BookAddRequest): BookDetailedDto {
        val authorList = getAuthorList(bookAddRequest)
        val publisher = getPublisher(bookAddRequest)

        val book = Book(
            isbn = bookAddRequest.isbn,
            title = bookAddRequest.title,
            summary = bookAddRequest.summary,
            publishedDate = bookAddRequest.publishedDate,
            detailUrl = bookAddRequest.detailUrl,
            translator = bookAddRequest.translator,
            price = bookAddRequest.price,
            titleImage = bookAddRequest.titleImage,
            publisher = publisher,
        )

        authorList.forEach { author ->
            book.addAuthor(author)
        }

        bookRepository.save(book)

        return BookDetailedDto.fromEntity(book)
    }

    private fun getAuthorList(bookAddRequest: BookAddRequest): MutableList<Author> {
        val authorList = mutableListOf<Author>()
        for (authorId in bookAddRequest.authorIdList) {
            val author = authorRepository.findById(authorId).getOrNull()
            if (author == null) {
                throw NotFoundException("Not found Author(authorId: $authorId)")
            }
            authorList.add(author)
        }

        for (authorName in bookAddRequest.authorNameList) {
            var author = authorRepository.findFirstByName(authorName).getOrNull()
            if (author == null) {
                author = authorRepository.save(Author(name = authorName))
            }
            authorList.add(author)
        }
        return authorList
    }

    private fun getPublisher(bookAddRequest: BookAddRequest): Publisher {
        val publisher = if (bookAddRequest.publisherId != null) {
            var publisher = publisherRepository.findById(bookAddRequest.publisherId).getOrNull()
            if (publisher == null) {
                throw NotFoundException("Not found Publisher Id: ${bookAddRequest.publisherId}")
            }
            publisher
        } else {
            var publisher = publisherRepository.findByName(bookAddRequest.publisherName!!)
            if (publisher == null) {
                publisher = publisherRepository.save(
                    Publisher(name = bookAddRequest.publisherName)
                )
            }
            publisher
        }
        return publisher
    }

}