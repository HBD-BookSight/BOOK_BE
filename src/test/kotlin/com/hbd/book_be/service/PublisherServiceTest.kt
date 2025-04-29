package com.hbd.book_be.service

import com.hbd.book_be.domain.Book
import com.hbd.book_be.domain.Publisher
import com.hbd.book_be.domain.Tag
import com.hbd.book_be.domain.common.UrlInfo
import com.hbd.book_be.dto.request.PublisherCreateRequest
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.repository.BookRepository
import com.hbd.book_be.repository.PublisherRepository
import com.hbd.book_be.repository.TagRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.Optional

@ActiveProfiles("test")
class PublisherServiceTest {

    private val publisherRepository: PublisherRepository = mockk()
    private val bookRepository: BookRepository = mockk()
    private val tagRepository: TagRepository = mockk()
    private val publisherService = PublisherService(publisherRepository, bookRepository, tagRepository)

    @Test
    @DisplayName("Publisher 목록을 조회해야 한다.")
    fun getPublishersTest() {
        // given
        val publisher = Publisher(
            id = 1L,
            name = "PublisherA",
            engName = "PublisherA-ENG",
            logo = "logo.png",
            description = "Some description",
            urls = mutableListOf()
        )
        every { publisherRepository.findAllActive(any()) } returns PageImpl(listOf(publisher))

        // when
        val result = publisherService.getPublishers()

        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].name).isEqualTo("PublisherA")
    }

    @Test
    @DisplayName("존재하는 Publisher 상세정보를 조회해야 한다.")
    fun getPublisherDetailTest() {
        // given
        val publisher = Publisher(
            id = 1L,
            name = "PublisherA",
            engName = "PublisherA-ENG",
            logo = "logo.png",
            description = "Some description",
            urls = mutableListOf()
        )
        every { publisherRepository.findById(1L) } returns Optional.of(publisher)

        // when
        val result = publisherService.getPublisherDetail(1L)

        // then
        assertThat(result.name).isEqualTo("PublisherA")
    }

    @Test
    @DisplayName("존재하지 않는 Publisher 조회 시 예외를 던져야 한다.")
    fun getPublisherDetailNotFoundTest() {
        // given
        every { publisherRepository.findById(1L) } returns Optional.empty()

        // when & then
        assertThatThrownBy { publisherService.getPublisherDetail(1L) }
            .isInstanceOf(NotFoundException::class.java)
    }

    @Test
    @DisplayName("Publisher를 생성해야 한다.")
    fun createPublisherTest() {
        // given
        val request = PublisherCreateRequest(
            name = "PublisherA",
            engName = "Publisher A",
            description = "Description",
            logo = "logo.png",
            memo = "Some memo",
            urls = listOf(
                UrlInfo(
                    url = "http://example.com",
                    type = "example"
                )
            ),
            tagList = listOf("Tag1", "Tag2"),
            bookIsbnList = listOf("123", "456")
        )

        val dummyPublisherForBook = Publisher(
            id = 2L,
            name = "DummyPublisher",
            engName = "DummyPublisher-ENG",
            logo = "dummy_logo.png",
            description = "This is a dummy publisher",
            urls = mutableListOf()
        )

        val book1 = Book(
            isbn = "1234567890123",
            title = "Test Book 1",
            summary = "A very interesting summary.",
            publishedDate = LocalDateTime.now(),
            detailUrl = "http://detail1.com",
            translator = listOf("Translator1", "Translator2"),
            price = 20000,
            titleImage = "http://image1.com",
            status = "PUBLISHED",
            publisher = dummyPublisherForBook
        )

        val book2 = Book(
            isbn = "9876543210987",
            title = "Test Book 2",
            summary = "Another interesting summary.",
            publishedDate = LocalDateTime.now(),
            detailUrl = "http://detail2.com",
            translator = listOf("TranslatorA"),
            price = 25000,
            titleImage = "http://image2.com",
            status = "PUBLISHED",
            publisher = dummyPublisherForBook
        )

        val tag1 = Tag(id = 1L, name = "Tag1")
        val tag2 = Tag(id = 2L, name = "Tag2")

        val savedPublisher = Publisher(
            id = 100L,
            name = request.name,
            engName = request.engName,
            description = request.description,
            logo = request.logo,
            urls = request.urls.toMutableList(),
            isOfficial = true
        )

        every { publisherRepository.findByName("PublisherA") } returns null
        every { tagRepository.findByName("Tag1") } returns null
        every { tagRepository.findByName("Tag2") } returns null
        every { tagRepository.save(any()) } returnsMany listOf(tag1, tag2)
        every { bookRepository.findAllById(any<List<String>>()) } returns listOf(book1, book2)
        every { publisherRepository.save(any()) } returns savedPublisher

        // when
        val result = publisherService.createPublisher(request)

        // then
        assertThat(result).isNotNull
        assertThat(result.name).isEqualTo("PublisherA")
        assertThat(result.id).isEqualTo(100L)
    }
}
