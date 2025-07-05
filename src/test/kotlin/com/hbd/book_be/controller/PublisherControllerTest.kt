package com.hbd.book_be.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.hbd.book_be.controller.api.PublisherController
import com.hbd.book_be.domain.common.UrlInfo
import com.hbd.book_be.dto.*
import com.hbd.book_be.dto.request.PublisherCreateRequest
import com.hbd.book_be.dto.request.enums.PublisherSortBy
import com.hbd.book_be.dto.request.enums.SortDirection
import com.hbd.book_be.service.PublisherService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
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

@WebMvcTest(
    controllers = [PublisherController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
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
        // given
        val publishers = listOf(
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
            ),
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
            )
        )

        val pageRequest = PageRequest.of(0, 10)
        val page = PageImpl(publishers, pageRequest, publishers.size.toLong())

        every { publisherService.getPublishers(0, 10, PublisherSortBy.Name, SortDirection.desc) } returns page

        mockMvc.perform(get("/api/v1/publishers"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.size()").value(2))
            .andExpect(jsonPath("$.items[0].name").value("Publisher B"))  // Mock 데이터 순서에 맞게
            .andExpect(jsonPath("$.items[1].name").value("Publisher A"))
            .andExpect(jsonPath("$.totalCount").value(2))
            .andExpect(jsonPath("$.totalPages").value(1))
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

        every { publisherService.getPublishers(0, 10, PublisherSortBy.Name, SortDirection.desc) } returns page

        // when + then
        mockMvc.perform(
            get("/api/v1/publishers")
                .param("orderBy", "Name")  // enum name 사용
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
