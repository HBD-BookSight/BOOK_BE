package com.hbd.book_be.service

import com.hbd.book_be.domain.Author
import com.hbd.book_be.domain.Book
import com.hbd.book_be.dto.AuthorDto
import com.hbd.book_be.dto.request.AuthorCreateRequest
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.repository.AuthorRepository
import com.hbd.book_be.repository.BookRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthorService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository
) {

    @Transactional(readOnly = true)
    fun getAuthors(page: Int, limit: Int): Page<AuthorDto> {
        val pageRequest = PageRequest.of(page, limit, Sort.by(Sort.Direction.ASC, "name"))
        val officialAuthorPage = authorRepository.findAllActiveOfficialAuthors(pageRequest)
        return officialAuthorPage.map { AuthorDto.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    fun getAuthor(authorId: Long): AuthorDto {
        val author = authorRepository.findByIdOrNull(authorId)
        if (author == null) {
            throw NotFoundException("Not found Author(id=$authorId)")
        }
        return AuthorDto.fromEntity(author)
    }

    @Transactional
    fun createAuthor(authorCreateRequest: AuthorCreateRequest): AuthorDto {
        val bookList = bookRepository.findAllById(authorCreateRequest.bookIsdnList)

        var author = Author(
            name = authorCreateRequest.name,
            description = authorCreateRequest.description,
            profile = authorCreateRequest.profile,
            isOfficial = true
        )
        bookList.forEach { updateBookAuthor(it, author) }

        author = authorRepository.save(author)
        return AuthorDto.fromEntity(author)
    }

    /***
     * Update BookAuthor
     * - find unofficial author in book and remove it
     * - add new official author to the book
     */
    private fun updateBookAuthor(
        book: Book,
        author: Author
    ) {
        val unOfficialBookAuthor = book.bookAuthorList.find { it.author.name == author.name && !it.author.isOfficial }
        if (unOfficialBookAuthor != null) {
            book.removeAuthor(unOfficialBookAuthor.author)
        }
        book.addAuthor(author)
    }

}