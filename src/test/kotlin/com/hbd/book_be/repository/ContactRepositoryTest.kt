package com.hbd.book_be.repository

import com.hbd.book_be.domain.Contact
import com.hbd.book_be.config.JpaConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@Import(JpaConfig::class)
@ActiveProfiles("test")
class ContactRepositoryTest {

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    @DisplayName("Contact를 저장하고 조회해야 한다.")
    fun saveAndFindContact() {
        // given
        val contact = Contact(
            name = "John Doe",
            email = "john@example.com",
            message = "Hello!"
        )
        entityManager.persist(contact)

        // when
        entityManager.flush()
        entityManager.clear()

        val foundContact = contactRepository.findById(contact.id!!).orElse(null)

        // then
        assertThat(foundContact).isNotNull
        assertThat(foundContact?.name).isEqualTo("John Doe")
        assertThat(foundContact?.email).isEqualTo("john@example.com")
        assertThat(foundContact?.message).isEqualTo("Hello!")
    }

    @Test
    @DisplayName("Contact를 createdAt 기준으로 내림차순 정렬해서 조회해야 한다.")
    fun findAllOrderByCreatedAtDesc() {
        // given
        val olderContact = Contact(
            name = "Old User",
            email = "old@example.com",
            message = "I'm old!"
        )
        olderContact.createdAt = LocalDateTime.now().minusDays(2)

        val newerContact = Contact(
            name = "New User",
            email = "new@example.com",
            message = "I'm new!"
        )
        newerContact.createdAt = LocalDateTime.now()

        entityManager.persist(olderContact)
        entityManager.persist(newerContact)

        // when
        entityManager.flush()
        entityManager.clear()

        val contacts = contactRepository.findAllByOrderByCreatedAtDesc()

        // then
        assertThat(contacts).hasSize(2)
        assertThat(contacts[0].email).isEqualTo("new@example.com")
        assertThat(contacts[1].email).isEqualTo("old@example.com")
    }
}
