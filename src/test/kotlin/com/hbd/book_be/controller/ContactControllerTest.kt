package com.hbd.book_be.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.hbd.book_be.dto.ContactDto
import com.hbd.book_be.dto.request.ContactCreateRequest
import com.hbd.book_be.service.ContactService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(ContactController::class)
@Import(ContactControllerTest.MockConfig::class)
@ActiveProfiles("test")
class ContactControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var contactService: ContactService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `GET contacts - success`() {
        // given
        val contacts = listOf(
            ContactDto(name = "John Doe", email = "john@example.com", message = "Hi there"),
            ContactDto(name = "Jane Doe", email = "jane@example.com", message = "Hello!")
        )
        every { contactService.getContacts() } returns contacts

        // when + then
        mockMvc.get("/api/v1/contacts")
            .andExpect {
                status { isOk() }
                jsonPath("$.items.size()") { value(2) }
                jsonPath("$.items[0].name") { value("John Doe") }
                jsonPath("$.items[1].name") { value("Jane Doe") }
                jsonPath("$.length") { value(2) }
            }
    }

    @Test
    fun `POST contact - success`() {
        // given
        val request =
            ContactCreateRequest(name = "New Contact", email = "new@example.com", message = "Nice to meet you!")
        val response = ContactDto(name = "New Contact", email = "new@example.com", message = "Nice to meet you!")
        every { contactService.createContact(request) } returns response

        // when + then
        mockMvc.post("/api/v1/contacts") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.name") { value("New Contact") }
                jsonPath("$.email") { value("new@example.com") }
                jsonPath("$.message") { value("Nice to meet you!") }
            }
    }

    @Configuration
    class MockConfig {
        @Bean
        fun contactService(): ContactService = mockk()
    }
}
