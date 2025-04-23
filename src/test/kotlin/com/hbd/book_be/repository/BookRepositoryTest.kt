package com.hbd.book_be.repository

import com.hbd.book_be.config.JpaConfig
import com.hbd.book_be.domain.Author
import com.hbd.book_be.domain.Book
import com.hbd.book_be.domain.Publisher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@Import(JpaConfig::class)
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    lateinit var bookRepository: BookRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    @DisplayName("책이 정상적으로 저장되어야 한다.")
    fun saveBookTest() {
        // given
        val author = Author(
            name = "test_author",
            description = null,
            profile = null,
        )
        entityManager.persist(author)

        val publisher = Publisher(
            name = "test_publisher",
            description = null,
            engName = null,
            logo = null
        )
        entityManager.persist(publisher)

        val book = Book(
            title = "test book",
            isbn = "1234",
            price = 25000,
            summary = "summary",
            publisher = publisher,
            publishedDate = LocalDateTime.now(),
            detailUrl = null,
            translator = null,
            titleImage = null,
        )
        book.addAuthor(author)

        // when
        val savedBook = bookRepository.save(book)
        entityManager.flush() // 변경사항을 DB에 반영
        entityManager.clear() // 영속성 컨텍스트 초기화

        val foundBook = bookRepository.findById(savedBook.isbn).get()

        // then
        assertThat(foundBook.isbn).isEqualTo("1234")
        assertThat(foundBook.title).isEqualTo("test book")

        // verify publisher relation
        assertThat(foundBook.publisher.id).isNotNull()
        assertThat(foundBook.publisher.name).isEqualTo("test_publisher")

        // verify author relation
        val savedBookAuthor = foundBook.bookAuthorList.first()
        assertThat(savedBookAuthor.author.id).isNotNull()
        assertThat(savedBookAuthor.author.name).isEqualTo("test_author")
    }

    @Test
    @DisplayName("책 조회 시 삭제되지 않은 책만 조회해야 한다.")
    fun findOnlyAliveBooksTest() {
        // given
        val author = Author(
            name = "test_author",
            description = null,
            profile = null,
        )
        entityManager.persist(author)

        val publisher = Publisher(
            name = "test_publisher",
            description = null,
            engName = null,
            logo = null
        )
        entityManager.persist(publisher)

        val book1 = Book(
            title = "test book 1",
            isbn = "1234",
            price = 25000,
            summary = "summary",
            publisher = publisher,
            publishedDate = LocalDateTime.now(),
            detailUrl = null,
            translator = null,
            titleImage = null,
        )
        book1.addAuthor(author)

        val book2 = Book(
            title = "test book 2",
            isbn = "5678",
            price = 25000,
            summary = "summary",
            publisher = publisher,
            publishedDate = LocalDateTime.now(),
            detailUrl = null,
            translator = null,
            titleImage = null,

            )
        book2.deletedAt = LocalDateTime.now()
        book2.addAuthor(author)

        // when
        bookRepository.saveAll(listOf(book1, book2))
        entityManager.flush() // 변경사항을 DB에 반영
        entityManager.clear() // 영속성 컨텍스트 초기화

        val pageRequest = PageRequest.of(0, 10)
        val findAllActive = bookRepository.findAllActive(keyword = null, pageable = pageRequest).content

        // then
        assertThat(findAllActive.size).isEqualTo(1)
        assertThat(findAllActive.first().title).isEqualTo("test book 1")
    }

    @Test
    @DisplayName("정렬된 순서로 책 조회해야 한다.")
    fun findSortedBooksTest() {
        // given
        val author = Author(
            name = "test_author",
            description = null,
            profile = null,
        )
        entityManager.persist(author)

        val publisher = Publisher(
            name = "test_publisher",
            description = null,
            engName = null,
            logo = null
        )
        entityManager.persist(publisher)

        val book1 = Book(
            title = "test book 1",
            isbn = "1234",
            price = 25000,
            summary = "summary",
            publisher = publisher,
            publishedDate = LocalDateTime.now(),
            detailUrl = null,
            translator = null,
            titleImage = null,
        )
        book1.addAuthor(author)

        val book2 = Book(
            title = "test book 2",
            isbn = "5678",
            price = 25000,
            summary = "summary",
            publisher = publisher,
            publishedDate = LocalDateTime.now(),
            detailUrl = null,
            translator = null,
            titleImage = null,
        )
        book2.addAuthor(author)

        // when
        bookRepository.saveAll(listOf(book1, book2))
        entityManager.flush() // 변경사항을 DB에 반영
        entityManager.clear() // 영속성 컨텍스트 초기화

        val descSortingRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "title"))
        val descSortedBooks = bookRepository.findAllActive(keyword = null, pageable = descSortingRequest).content

        val ascSortingRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "title"))
        val ascSortedBooks = bookRepository.findAllActive(keyword = null, pageable = ascSortingRequest).content

        // then
        assertThat(descSortedBooks.size).isEqualTo(2)
        assertThat(ascSortedBooks.size).isEqualTo(2)

        assertThat(descSortedBooks.first().title).isEqualTo("test book 2")
        assertThat(descSortedBooks.last().title).isEqualTo("test book 1")

        assertThat(ascSortedBooks.first().title).isEqualTo("test book 1")
        assertThat(ascSortedBooks.last().title).isEqualTo("test book 2")
    }

    @Test
    @DisplayName("책 이름으로 조회가 가능해야 한다.")
    fun searchByBookTitle() {
        // given
        val author = Author(
            name = "test_author",
            description = null,
            profile = null,
        )
        entityManager.persist(author)

        val publisher = Publisher(
            name = "test_publisher",
            description = null,
            engName = null,
            logo = null
        )
        entityManager.persist(publisher)

        val book1 = Book(
            title = "first book",
            isbn = "1234",
            price = 25000,
            summary = "summary",
            publisher = publisher,
            publishedDate = LocalDateTime.now(),
            detailUrl = null,
            translator = null,
            titleImage = null,
        )
        book1.addAuthor(author)

        val book2 = Book(
            title = "second book",
            isbn = "5678",
            price = 25000,
            summary = "summary",
            publisher = publisher,
            publishedDate = LocalDateTime.now(),
            detailUrl = null,
            translator = null,
            titleImage = null,
        )
        book2.addAuthor(author)

        // when
        bookRepository.saveAll(listOf(book1, book2))
        entityManager.flush() // 변경사항을 DB에 반영
        entityManager.clear() // 영속성 컨텍스트 초기화

        val pageRequest = PageRequest.of(0, 10)
        val foundBooks = bookRepository.findAllActive(keyword = "first", pageable = pageRequest).content

        // then
        assertThat(foundBooks.size).isEqualTo(1)
        assertThat(foundBooks.first().title).isEqualTo("first book")
    }

    @Test
    @DisplayName("작가 이름으로 조회가 가능해야 한다.")
    fun searchByAuthorName() {
        // given
        val firstAuthor = Author(
            name = "first_author",
            description = null,
            profile = null,
        )
        entityManager.persist(firstAuthor)

        val secondAuthor = Author(
            name = "second_author",
            description = null,
            profile = null,
        )
        entityManager.persist(secondAuthor)

        val publisher = Publisher(
            name = "test_publisher",
            description = null,
            engName = null,
            logo = null
        )
        entityManager.persist(publisher)

        val book1 = Book(
            title = "first book",
            isbn = "1234",
            price = 25000,
            summary = "summary",
            publisher = publisher,
            publishedDate = LocalDateTime.now(),
            detailUrl = null,
            translator = null,
            titleImage = null,
        )
        book1.addAuthor(firstAuthor)

        val book2 = Book(
            title = "second book",
            isbn = "5678",
            price = 25000,
            summary = "summary",
            publisher = publisher,
            publishedDate = LocalDateTime.now(),
            detailUrl = null,
            translator = null,
            titleImage = null,
        )
        book2.addAuthor(secondAuthor)

        // when
        bookRepository.saveAll(listOf(book1, book2))
        entityManager.flush() // 변경사항을 DB에 반영
        entityManager.clear() // 영속성 컨텍스트 초기화

        val pageRequest = PageRequest.of(0, 10)
        val foundBooks = bookRepository.findAllActive(keyword = "first_author", pageable = pageRequest).content

        // then
        assertThat(foundBooks.size).isEqualTo(1)
        assertThat(foundBooks.first().title).isEqualTo("first book")
        assertThat(foundBooks.first().bookAuthorList.size).isEqualTo(1)
        assertThat(foundBooks.first().bookAuthorList.first().author.name).isEqualTo("first_author")
    }

    @Test
    @DisplayName("출판사 이름으로 조회가 가능해야 한다.")
    fun searchByPublisherName() {
        // given
        val author = Author(
            name = "test_author",
            description = null,
            profile = null,
        )
        entityManager.persist(author)

        val firstPublisher = Publisher(
            name = "first_publisher",
            description = null,
            engName = null,
            logo = null
        )
        entityManager.persist(firstPublisher)

        val secondPublisher = Publisher(
            name = "second_publisher",
            description = null,
            engName = null,
            logo = null
        )
        entityManager.persist(secondPublisher)

        val book1 = Book(
            title = "first book",
            isbn = "1234",
            price = 25000,
            summary = "summary",
            publisher = firstPublisher,
            publishedDate = LocalDateTime.now(),
            detailUrl = null,
            translator = null,
            titleImage = null,
        )
        book1.addAuthor(author)

        val book2 = Book(
            title = "second book",
            isbn = "5678",
            price = 25000,
            summary = "summary",
            publisher = secondPublisher,
            publishedDate = LocalDateTime.now(),
            detailUrl = null,
            translator = null,
            titleImage = null,
        )
        book2.addAuthor(author)

        // when
        bookRepository.saveAll(listOf(book1, book2))
        entityManager.flush() // 변경사항을 DB에 반영
        entityManager.clear() // 영속성 컨텍스트 초기화

        val pageRequest = PageRequest.of(0, 10)
        val foundBooks = bookRepository.findAllActive(keyword = "first_publisher", pageable = pageRequest).content

        // then
        assertThat(foundBooks.size).isEqualTo(1)
        assertThat(foundBooks.first().title).isEqualTo("first book")
        assertThat(foundBooks.first().publisher.name).isEqualTo("first_publisher")
    }

    @Test
    @DisplayName("검색 시 작가 이름, 출판사 이름, 책 이름 등을 모두 고려해서 반환해야 한다.")
    fun searchTotal() {
        // given
        val targetAuthor = Author(
            name = "target author",
            description = null,
            profile = null,
        )
        entityManager.persist(targetAuthor)

        val otherAuthor = Author(
            name = "other author",
            description = null,
            profile = null,
        )
        entityManager.persist(otherAuthor)

        val targetPublisher = Publisher(
            name = "target publisher",
            description = null,
            engName = null,
            logo = null
        )
        entityManager.persist(targetPublisher)

        val otherPublisher = Publisher(
            name = "other publisher",
            description = null,
            engName = null,
            logo = null
        )
        entityManager.persist(otherPublisher)


        val targetBook1 = Book(
            title = "target book",
            isbn = "1111",
            price = 25000,
            summary = "summary",
            publisher = otherPublisher,
            publishedDate = LocalDateTime.now(),
            detailUrl = null,
            translator = null,
            titleImage = null,
        )
        targetBook1.addAuthor(otherAuthor)

        val targetBook2 = Book(
            title = "other book",
            isbn = "2222",
            price = 25000,
            summary = "summary",
            publisher = targetPublisher,
            publishedDate = LocalDateTime.now(),
            detailUrl = null,
            translator = null,
            titleImage = null,
        )
        targetBook2.addAuthor(otherAuthor)

        val targetBook3 = Book(
            title = "other book",
            isbn = "3333",
            price = 25000,
            summary = "summary",
            publisher = otherPublisher,
            publishedDate = LocalDateTime.now(),
            detailUrl = null,
            translator = null,
            titleImage = null,
        )
        targetBook3.addAuthor(targetAuthor)

        val otherBook = Book(
            title = "other book",
            isbn = "4444",
            price = 25000,
            summary = "summary",
            publisher = otherPublisher,
            publishedDate = LocalDateTime.now(),
            detailUrl = null,
            translator = null,
            titleImage = null,
        )
        otherBook.addAuthor(otherAuthor)

        // when
        bookRepository.saveAll(listOf(targetBook1, targetBook2, targetBook3, otherBook))
        entityManager.flush() // 변경사항을 DB에 반영
        entityManager.clear() // 영속성 컨텍스트 초기화

        val pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "isbn"))
        val foundBooks = bookRepository.findAllActive(keyword = "target", pageable = pageRequest).content

        // then
        assertThat(foundBooks.size).isEqualTo(3)

        assertThat(foundBooks[0].isbn).isEqualTo("1111")
        assertThat(foundBooks[0].title).isEqualTo("target book")

        assertThat(foundBooks[1].isbn).isEqualTo("2222")
        assertThat(foundBooks[1].title).isEqualTo("other book")
        assertThat(foundBooks[1].publisher.name).isEqualTo("target publisher")

        assertThat(foundBooks[2].isbn).isEqualTo("3333")
        assertThat(foundBooks[2].title).isEqualTo("other book")
        assertThat(foundBooks[2].publisher.name).isEqualTo("other publisher")
        assertThat(foundBooks[2].bookAuthorList.size).isEqualTo(1)
        assertThat(foundBooks[2].bookAuthorList.first().author.name).isEqualTo("target author")
    }
}