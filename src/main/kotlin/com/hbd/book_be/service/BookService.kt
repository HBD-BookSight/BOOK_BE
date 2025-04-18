package com.hbd.book_be.service

import com.hbd.book_be.domain.Author
import com.hbd.book_be.domain.Book
import com.hbd.book_be.domain.Publisher
import com.hbd.book_be.dto.*
import com.hbd.book_be.dto.request.BookCreateRequest
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.repository.AuthorRepository
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.repository.PublisherRepository
import com.hbd.book_be.repository.RecommendedBookRepository
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

    @Autowired
    private val recommendedBookRepository: RecommendedBookRepository
) {
    @Transactional(readOnly = true)
    fun getBooks(page: Int, limit: Int, orderBy: String, direction: String): Page<BookDto> {
        val sortDirection = Sort.Direction.fromString(direction)
        val sort = Sort.by(sortDirection, orderBy)
        val pageRequest = PageRequest.of(page, limit, sort)

        val bookPage = bookRepository.findAllActive(pageRequest)
        return bookPage.map { BookDto.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    fun getBookDetail(isbn: String): BookDto.Detail {
        val book = bookRepository.findById(isbn).getOrNull()
        if (book == null || book.deletedAt != null) {
            throw NotFoundException("Not found Book(isbn: $isbn)")
        }

        return BookDto.Detail.fromEntity(book)
    }

    @Transactional
    fun createBook(bookCreateRequest: BookCreateRequest): BookDto.Detail {
        val authorList = getOrCreateAuthorList(bookCreateRequest)
        val publisher = getOrCreatePublisher(bookCreateRequest)

        val book = Book(
            isbn = bookCreateRequest.isbn,
            title = bookCreateRequest.title,
            summary = bookCreateRequest.summary,
            publishedDate = bookCreateRequest.publishedDate,
            detailUrl = bookCreateRequest.detailUrl,
            translator = bookCreateRequest.translator,
            price = bookCreateRequest.price,
            titleImage = bookCreateRequest.titleImage,
            publisher = publisher,
        )

        authorList.forEach { author ->
            book.addAuthor(author)
        }

        bookRepository.save(book)

        return BookDto.Detail.fromEntity(book)
    }

    @Transactional(readOnly = true)
    fun getRecommendedBooks(): List<RecommendedBookDto> {
        val recommendedBookList = recommendedBookRepository.findRecentRecommendedBooks()
        return recommendedBookList.map { RecommendedBookDto.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    fun getBookEventList(isbn: String): List<EventDto> {
        val book = bookRepository.findById(isbn).getOrNull()
        if (book == null) {
            throw NotFoundException("Not found Book(isbn: $isbn)")
        }

        return book.bookEventList.map {
            EventDto.fromEntity(it.event)
        }
    }

    @Transactional(readOnly = true)
    fun getBookContentsList(isbn: String): List<ContentsDto> {
        val book = bookRepository.findById(isbn).getOrNull()
        if (book == null) {
            throw NotFoundException("Not found Book(isbn: $isbn)")
        }

        return book.bookContentsList.map {
            ContentsDto.fromEntity(it.contents)
        }
    }

    private fun getOrCreateAuthorList(bookCreateRequest: BookCreateRequest): List<Author> {
        val authorList = mutableListOf<Author>()
        for (authorId in bookCreateRequest.authorIdList) {
            val author = authorRepository.findById(authorId).getOrNull()
            if (author == null) {
                throw NotFoundException("Not found Author(authorId: $authorId)")
            }
            authorList.add(author)
        }

        for (authorName in bookCreateRequest.authorNameList) {
            var author = authorRepository.findFirstByName(authorName).getOrNull()
            if (author == null) {
                author = authorRepository.save(
                    Author(
                        name = authorName,
                        description = null,
                        profile = null
                    )
                )
            }
            authorList.add(author)
        }
        return authorList
    }

    private fun getOrCreatePublisher(bookCreateRequest: BookCreateRequest): Publisher {
        val publisher = if (bookCreateRequest.publisherId != null) {
            var publisher = publisherRepository.findById(bookCreateRequest.publisherId).getOrNull()
            if (publisher == null) {
                throw NotFoundException("Not found Publisher Id: ${bookCreateRequest.publisherId}")
            }
            publisher
        } else {
            var publisher = publisherRepository.findByName(bookCreateRequest.publisherName!!)
            if (publisher == null) {
                publisher = publisherRepository.save(
                    Publisher(
                        name = bookCreateRequest.publisherName,
                        engName = null,
                        logo = null,
                        description = null
                    )
                )
            }
            publisher
        }
        return publisher
    }


}