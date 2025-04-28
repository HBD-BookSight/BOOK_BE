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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest
@Import(ContactController::class)
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
        this.mockMvc.perform(get("/api/v1/contacts"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.size()").value(2))
            .andExpect(jsonPath("$.items[0].name").value("John Doe"))
            .andExpect(jsonPath("$.items[1].name").value("Jane Doe"))
            .andExpect(jsonPath("$.length").value(2))
    }

    @Test
    fun `POST contact - success`() {
        // given
        val request = ContactCreateRequest(
            name = "New Contact",
            email = "new@example.com",
            message = "Nice to meet you!"
        )
        val response = ContactDto(
            name = "New Contact",
            email = "new@example.com",
            message = "Nice to meet you!"
        )
        every { contactService.createContact(request) } returns response

        val contactCreateRequest = ContactCreateRequest(
            name = "New Contact",
            email = "new@example.com",
            message = "Nice to meet you!"
        )
        // when + then
        this.mockMvc.perform(
            post("/api/v1/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactCreateRequest))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("New Contact"))
            .andExpect(jsonPath("$.email").value("new@example.com"))
            .andExpect(jsonPath("$.message").value("Nice to meet you!"))
    }

    @Configuration
    class MockConfig {
        @Bean
        fun contactService(): ContactService = mockk(relaxed = true)
    }
}
