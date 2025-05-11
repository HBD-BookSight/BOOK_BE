package com.hbd.book_be.service

import com.hbd.book_be.domain.Contact
import com.hbd.book_be.dto.request.ContactCreateRequest
import com.hbd.book_be.repository.ContactRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
class ContactServiceTest {

    private val contactRepository: ContactRepository = mockk()
    private val contactService = ContactService(contactRepository)

    @Test
    @DisplayName("Contact를 생성해야 한다.")
    fun createContactTest() {
        val request = ContactCreateRequest(
            name = "John Doe",
            email = "john@example.com",
            message = "Hello!"
        )

        every { contactRepository.save(any()) } returns Contact(
            id = 1L,
            name = request.name,
            email = request.email,
            message = request.message
        )

        val result = contactService.createContact(request)

        assertThat(result.name).isEqualTo("John Doe")
        assertThat(result.email).isEqualTo("john@example.com")
        assertThat(result.message).isEqualTo("Hello!")
    }

    @Test
    @DisplayName("Contact를 createdAt 기준으로 최신순 조회해야 한다.")
    fun getContactsTest() {
        val contacts = listOf(
            Contact(id = 1L, name = "User1", email = "user1@example.com", message = "Message1"),
            Contact(id = 2L, name = "User2", email = "user2@example.com", message = "Message2")
        )

        every { contactRepository.findAllByOrderByCreatedAtDesc() } returns contacts

        val result = contactService.getContacts()

        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("User1")
        assertThat(result[1].name).isEqualTo("User2")
    }
}
