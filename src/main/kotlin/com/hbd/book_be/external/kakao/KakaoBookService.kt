package com.hbd.book_be.external.kakao

import com.hbd.book_be.dto.BookDto
import com.hbd.book_be.dto.request.BookCreateRequest
import com.hbd.book_be.exception.KakaoBookInfoNotFoundException
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.service.BookService
import org.springframework.beans.factory.annotation.Autowired
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

        val response = kakaoBookSearchClient.searchBook(request)
            ?: throw KakaoBookInfoNotFoundException(
                message = "Kakao API에서 책 정보를 찾을 수 없습니다."
            )

        val isbnList = response.documents.flatMap { it.isbn.split(" ") }
        val existingIsbns = bookRepository.findByIsbnIn(isbnList).map { it.isbn }.toSet()

        return response.documents.map { document ->
            val isbnSplit = document.isbn.split(" ")
            val isbn10 = isbnSplit.find { it.length == 10 }
            val isbn13 = isbnSplit.find { it.length == 13 }

            KakaoBookDto.fromKakaoApi(
                document = document,
                isIsbn10Exist = isbn10?.let { existingIsbns.contains(it) } ?: false,
                isIsbn13Exist = isbn13?.let { existingIsbns.contains(it) } ?: false
            )
        }
    }


    @Transactional
    fun createBook(isbn: String): BookDto.Detail {
        val request = KakaoApiRequest(query = isbn, target = "isbn")
        val response = kakaoBookSearchClient.searchBook(request)
            ?: throw KakaoBookInfoNotFoundException(
                message = "Kakao API에서 책 정보를 찾을 수 없습니다.",
            )

        if (response.documents.isEmpty()) {
            throw NotFoundException("Not found Book(isbn: $isbn)")
        }

        val bookCreateRequest = mapToBookCreateRequest(response.documents.first())
        return bookService.createBook(bookCreateRequest)
    }

    private fun mapToBookCreateRequest(document: KakaoApiResponse.Document): BookCreateRequest {
        return BookCreateRequest(
            isbn = document.isbn,
            title = document.title,
            summary = document.contents,
            publishedDate = parseDate(document.datetime),
            detailUrl = document.url,
            translator = document.translators,
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
