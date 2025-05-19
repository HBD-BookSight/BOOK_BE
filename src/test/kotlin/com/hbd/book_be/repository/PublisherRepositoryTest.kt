package com.hbd.book_be.repository

import com.hbd.book_be.config.JpaConfig
import com.hbd.book_be.domain.Publisher
import com.hbd.book_be.domain.Tag
import com.hbd.book_be.domain.common.UrlInfo
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
class PublisherRepositoryTest {

    @Autowired
    lateinit var publisherRepository: PublisherRepository

    @Autowired
    lateinit var tagRepository: TagRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    @DisplayName("Publisher 저장 및 단일 조회")
    fun saveAndFindPublisher() {
        val publisher = Publisher(
            name = "Test Publisher",
            engName = "Test Publisher ENG",
            logo = "logo.png",
            description = "Test description",
            urls = mutableListOf(UrlInfo(type = "homepage", url = "http://test.com")),
            isOfficial = true
        )

        val saved = publisherRepository.save(publisher)
        val found = publisherRepository.findById(saved.id!!).orElse(null)

        assertThat(found).isNotNull
        assertThat(found?.name).isEqualTo("Test Publisher")
        assertThat(found?.urls).hasSize(1)
        assertThat(found?.urls?.first()?.url).isEqualTo("http://test.com")
    }

    @Test
    @DisplayName("삭제되지 않은 Publisher만 조회")
    fun findAllActive() {
        val active = Publisher(
            name = "Active",
            engName = "ENG_ACTIVE",
            logo = null,
            description = "Active desc",
            urls = mutableListOf(),
            isOfficial = true
        )
        val deleted = Publisher(
            name = "Deleted",
            engName = "ENG_DELETED",
            logo = null,
            description = "Deleted desc",
            urls = mutableListOf(),
            isOfficial = false
        ).apply {
            deletedAt = LocalDateTime.now()
        }

        publisherRepository.saveAll(listOf(active, deleted))

        val result = publisherRepository.findAllActive(PageRequest.of(0, 10)).content

        assertThat(result).hasSize(1)
        assertThat(result.first().name).isEqualTo("Active")
    }

    @Test
    @DisplayName("Publisher 이름으로 조회")
    fun findByName() {
        val publisher = Publisher(
            name = "Unique Publisher",
            engName = "ENG_UNIQUE",
            logo = null,
            description = "description",
            urls = mutableListOf(),
            isOfficial = true
        )

        publisherRepository.save(publisher)
        val found = publisherRepository.findByName("Unique Publisher")

        assertThat(found).isNotNull
        assertThat(found?.description).isEqualTo("description")
    }

    @Test
    @DisplayName("Tag와 연결된 Publisher 저장")
    fun savePublisherWithTags() {
        val tag1 = Tag(name = "Tag1")
        val tag2 = Tag(name = "Tag2")
        tagRepository.saveAll(listOf(tag1, tag2))

        val publisher = Publisher(
            name = "Tagged Publisher",
            engName = "ENG",
            logo = null,
            description = "desc",
            urls = mutableListOf(),
            isOfficial = true
        )

        val savedPublisher = publisherRepository.save(publisher)

        // flush 및 clear로 세션 초기화 (중복 연관 방지)
        entityManager.flush()
        entityManager.clear()

        // 영속성 컨텍스트에서 다시 조회해서 연관관계 추가
        val managedPublisher = publisherRepository.findById(savedPublisher.id!!).get()
        val managedTag1 = tagRepository.findById(tag1.id!!).get()
        val managedTag2 = tagRepository.findById(tag2.id!!).get()

        managedPublisher.addTag(managedTag1)
        managedPublisher.addTag(managedTag2)

        entityManager.flush()
        entityManager.clear()

        val found = publisherRepository.findByName("Tagged Publisher")

        assertThat(found).isNotNull
        assertThat(found!!.tagPublisherList).hasSize(2)
        assertThat(found.tagPublisherList.map { it.tag.name }).containsExactlyInAnyOrder("Tag1", "Tag2")
    }

    @Test
    @DisplayName("이름 기준 오름차순 정렬된 Publisher 목록 조회")
    fun findAllActiveOrderByNameAsc() {
        val pubC = Publisher(
            name = "Charlie Publisher",
            engName = "ENG-C",
            logo = null,
            description = "Third",
            urls = mutableListOf(),
            isOfficial = true
        )
        val pubA = Publisher(
            name = "Alpha Publisher",
            engName = "ENG-A",
            logo = null,
            description = "First",
            urls = mutableListOf(),
            isOfficial = true
        )
        val pubB = Publisher(
            name = "Bravo Publisher",
            engName = "ENG-B",
            logo = null,
            description = "Second",
            urls = mutableListOf(),
            isOfficial = true
        )

        publisherRepository.saveAll(listOf(pubC, pubA, pubB))

        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"))
        val result = publisherRepository.findAllActive(pageable).content

        assertThat(result).hasSize(3)
        assertThat(result.map { it.name }).containsExactly("Alpha Publisher", "Bravo Publisher", "Charlie Publisher")
    }
}