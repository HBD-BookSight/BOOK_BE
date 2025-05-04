package com.hbd.book_be.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.hbd.book_be.domain.common.UrlInfo
import com.hbd.book_be.dto.*
import com.hbd.book_be.dto.request.PublisherCreateRequest
import com.hbd.book_be.service.PublisherService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest(PublisherController::class)
@Import(PublisherController::class)
@ActiveProfiles("test")
class PublisherControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var publisherService: PublisherService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `GET publishers - success`() {
        val publishers = listOf(
            PublisherDto(
                id = 1L,
                name = "Publisher A",
                engName = "Publisher A Eng",
                logo = null,
                isOfficial = true,
                description = "Description A",
                urls = listOf(
                    UrlInfo(type = "homepage", url = "http://publisher-a.com")
                )
            ),
            PublisherDto(
                id = 2L,
                name = "Publisher B",
                engName = "Publisher B Eng",
                logo = null,
                isOfficial = false,
                description = "Description B",
                urls = listOf(
                    UrlInfo(type = "homepage", url = "http://publisher-b.com")
                )
            )
        )

        val pageRequest = PageRequest.of(0, 10)
        val page = PageImpl(publishers, pageRequest, publishers.size.toLong())

        every { publisherService.getPublishers(0, 10, "name", "asc") } returns page

        mockMvc.perform(get("/api/v1/publishers"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.size()").value(2))
            .andExpect(jsonPath("$.items[0].name").value("Publisher A"))
            .andExpect(jsonPath("$.items[1].name").value("Publisher B"))
            .andExpect(jsonPath("$.totalCount").value(2))
            .andExpect(jsonPath("$.totalPages").value(1))
    }

    @Test
    fun `POST publisher - success`() {
        val request = PublisherCreateRequest(
            name = "New Publisher",
            engName = "New Publisher Eng",
            logo = null,
            description = "New description",
            memo = "Internal note"
        )

        val response = PublisherDto.Detail(
            id = 3L,
            name = "New Publisher",
            engName = "New Publisher Eng",
            logo = null,
            isOfficial = true,
            description = "New description",
            urls = listOf(
                UrlInfo(type = "homepage", url = "http://newpublisher.com")
            ),
            bookDtoList = listOf(
                BookDto(
                    isbn = "1234567890",
                    title = "Sample Book",
                    summary = "This is a book summary.",
                    publishedDate = LocalDateTime.now(),
                    titleImage = null,
                    authorList = listOf(
                        AuthorDto.Simple(
                            id = 1L,
                            name = "Author Name",
                        )
                    ),
                    translator = listOf("Translator A"),
                    price = 15000,
                    publisher = PublisherDto.Simple(
                        id = 3L,
                        name = "New Publisher"
                    )
                )
            ),
            tagDtoList = listOf(
                TagDto(name = "Fiction")
            )
        )

        every { publisherService.createPublisher(request) } returns response

        mockMvc.perform(
            post("/api/v1/publishers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(3L))
            .andExpect(jsonPath("$.name").value("New Publisher"))
    }

    @Test
    fun `GET detailed publisher - success`() {
        val detail = PublisherDto.Detail(
            id = 1L,
            name = "Publisher A",
            engName = "Publisher A Eng",
            logo = null,
            isOfficial = true,
            description = "Detail description",
            urls = listOf(
                UrlInfo(type = "homepage", url = "http://publisher-a.com")
            ),
            bookDtoList = listOf(
                BookDto(
                    isbn = "111122223333",
                    title = "Another Book",
                    summary = "A deep look at publishing.",
                    publishedDate = LocalDateTime.now(),
                    titleImage = null,
                    authorList = listOf(
                        AuthorDto.Simple(
                            id = 2L,
                            name = "Another Author",
                        )
                    ),
                    translator = listOf("Translator B"),
                    price = 18000,
                    publisher = PublisherDto.Simple(
                        id = 1L,
                        name = "Publisher A"
                    )
                )
            ),
            tagDtoList = listOf(
                TagDto(name = "Education")
            )
        )

        every { publisherService.getPublisherDetail(1L) } returns detail

        mockMvc.perform(get("/api/v1/publishers/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("Publisher A"))
    }

    @Test
    fun `GET publishers - 이름 기준 내림차순 정렬`() {
        // given
        val publishers = listOf(
            PublisherDto(
                id = 2L,
                name = "Publisher B",
                engName = "Publisher B Eng",
                logo = null,
                isOfficial = false,
                description = "Description B",
                urls = listOf(UrlInfo(type = "homepage", url = "http://publisher-b.com"))
            ),
            PublisherDto(
                id = 1L,
                name = "Publisher A",
                engName = "Publisher A Eng",
                logo = null,
                isOfficial = true,
                description = "Description A",
                urls = listOf(UrlInfo(type = "homepage", url = "http://publisher-a.com"))
            )
        )

        val pageRequest = PageRequest.of(0, 10)
        val page = PageImpl(publishers, pageRequest, publishers.size.toLong())

        every { publisherService.getPublishers(0, 10, "name", "desc") } returns page

        // when + then
        mockMvc.perform(
            get("/api/v1/publishers")
                .param("orderBy", "name")
                .param("direction", "desc")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.size()").value(2))
            .andExpect(jsonPath("$.items[0].name").value("Publisher B"))
            .andExpect(jsonPath("$.items[1].name").value("Publisher A"))
    }

    @Configuration
    class MockConfig {
        @Bean
        fun publisherService(): PublisherService = mockk(relaxed = true)
    }
}
