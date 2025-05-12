package com.hbd.book_be.external.kakao

import com.hbd.book_be.dto.AuthorDto
import com.hbd.book_be.dto.BookDto
import com.hbd.book_be.dto.PublisherDto
import com.hbd.book_be.dto.request.BookCreateRequest
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.service.BookService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class KakaoBookService(
    @Autowired
    private val kakaoBookSearchClient: KakaoBookSearchClient,
    private val bookService: BookService,
    private val bookRepository: BookRepository,
) {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    @Transactional(readOnly = true)
    fun searchBook(request: KakaoApiRequest): List<KakaoBookDto> {
        val pageRequest = PageRequest.of(request.page, request.size)
        val response = kakaoBookSearchClient.searchBook(request)
            ?: throw IllegalArgumentException("Kakao API에서 책 정보를 찾을 수 없습니다.")

        val globalIndex = pageRequest.pageNumber * pageRequest.pageSize + 1L
        return mapToBookDtos(response.documents, globalIndex)
    }

    @Transactional
    fun createBook(isbn: String): BookDto.Detail {
        val request = KakaoApiRequest(query = isbn, target = "isbn")
        val response = kakaoBookSearchClient.searchBook(request)
            ?: throw IllegalArgumentException("Kakao API에서 책 정보를 찾을 수 없습니다.")

        if (response.documents.isEmpty()) {
            throw IllegalArgumentException("해당 ISBN으로 조회된 책이 없습니다.")
        }

        val bookCreateRequest = mapToBookCreateRequest(response.documents.first())
        return bookService.createBook(bookCreateRequest)
    }

    private fun mapToBookDtos(documents: List<KakaoApiResponse.Document>, startId: Long): List<KakaoBookDto> {
        val isbnList = documents.map { it.isbn }
        val existingIsbns = bookRepository.findByIsbnIn(isbnList).map { it.isbn }.toSet()

        var globalIndex = startId
        return documents.map { document ->
            val authorList = document.authors.mapIndexed { index, author ->
                AuthorDto.Simple(id = globalIndex + index, name = author)
            }

            val book = BookDto(
                isbn = document.isbn,
                title = document.title,
                summary = document.contents,
                publishedDate = parseDate(document.datetime),
                titleImage = document.thumbnail,
                authorList = authorList,
                translator = document.translators.joinToString(", "),
                price = document.price,
                publisher = PublisherDto.Simple(id = 0L, name = document.publisher)
            )

            KakaoBookDto(
                bookDto = book,
                isExist = existingIsbns.contains(document.isbn)
            ).also {
                globalIndex++
            }
        }
    }

    private fun mapToBookCreateRequest(document: KakaoApiResponse.Document): BookCreateRequest {
        return BookCreateRequest(
            isbn = document.isbn,
            title = document.title,
            summary = document.contents,
            publishedDate = parseDate(document.datetime),
            detailUrl = document.url,
            translator = document.translators.joinToString(", "),
            price = document.price,
            titleImage = document.thumbnail,
            authorNameList = document.authors,
            publisherName = document.publisher
        )
    }

    private fun parseDate(dateString: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateString, formatter)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
}
