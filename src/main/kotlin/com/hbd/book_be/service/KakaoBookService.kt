package com.hbd.book_be.service

import com.hbd.book_be.client.KakaoBookSearchClient
import com.hbd.book_be.dto.AuthorDto
import com.hbd.book_be.dto.BookDto
import com.hbd.book_be.dto.PublisherDto
import com.hbd.book_be.dto.request.BookCreateRequest
import com.hbd.book_be.dto.request.KakaoBookRequest
import com.hbd.book_be.dto.response.KakaoBookResponse
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
    @Autowired
    private val bookService: BookService
) {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    @Transactional(readOnly = true)
    fun searchBook(request: KakaoBookRequest): List<BookDto> {
        val pageRequest = PageRequest.of(request.page ?: 0, request.size ?: 10)
        val response = kakaoBookSearchClient.searchBook(request)
            ?: throw IllegalArgumentException("Kakao API에서 책 정보를 찾을 수 없습니다.")

        var globalIndex = pageRequest.pageNumber * pageRequest.pageSize + 1L

        return response.documents.map {
            mapToBookDto(it, globalIndex++)
        }
    }

    @Transactional
    fun createBook(isbn: String): BookDto.Detail {
        val request = KakaoBookRequest(query = isbn, target = "isbn")
        val response = kakaoBookSearchClient.searchBook(request)
            ?: throw IllegalArgumentException("Kakao API에서 책 정보를 찾을 수 없습니다.")

        if (response.documents.isEmpty()) {
            throw IllegalArgumentException("해당 ISBN으로 조회된 책이 없습니다.")
        }

        val bookCreateRequest = mapToBookCreateRequest(response.documents.first())
        return bookService.createBook(bookCreateRequest)
    }

    private fun mapToBookDto(document: KakaoBookResponse.Document, id: Long): BookDto {
        val authorList = document.authors.mapIndexed { index, author ->
            AuthorDto.Simple(id = id + index, name = author)
        }

        return BookDto(
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
    }

    private fun mapToBookCreateRequest(document: KakaoBookResponse.Document): BookCreateRequest {
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
        return LocalDateTime.parse("${dateString.substring(0, 10)}T00:00:00", formatter)
    }
}
