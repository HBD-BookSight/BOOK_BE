package com.hbd.book_be.service

import com.hbd.book_be.domain.Contents
import com.hbd.book_be.domain.Tag
import com.hbd.book_be.dto.ContentsDto
import com.hbd.book_be.dto.DiscoveryContentsDto
import com.hbd.book_be.dto.request.ContentsCreateRequest
import com.hbd.book_be.dto.request.ContentsUpdateRequest
import com.hbd.book_be.dto.request.enums.ContentsSortBy
import com.hbd.book_be.dto.request.enums.SortDirection
import com.hbd.book_be.enums.UserRole
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.repository.*
import com.hbd.book_be.util.AuthUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class ContentsService(
    private val contentsRepository: ContentsRepository,
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository,
    private val bookRepository: BookRepository,
) {

    @Transactional(readOnly = true)
    fun getContentsDetail(id: Long): ContentsDto.Detail {
        val contents = contentsRepository.findById(id).getOrNull()
        if (contents == null || contents.deletedAt != null) {
            throw NotFoundException("Not found Cotents(isbn: $id)")
        }

        return ContentsDto.Detail.fromEntity(contents)
    }

    @Transactional(readOnly = true)
    fun getContents(
        page: Int,
        limit: Int,
        orderBy: ContentsSortBy,
        direction: SortDirection,
    ): Page<ContentsDto.Detail> {
        val sortDirection = Sort.Direction.fromString(direction.name)
        val sort = Sort.by(sortDirection, orderBy.value)
        val pageRequest = PageRequest.of(page, limit, sort)

        val contentsPage = contentsRepository.findAllActive(pageRequest)
        return contentsPage.map { ContentsDto.Detail.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    fun getDiscoveryContents(
    ): List<ContentsDto> {
        val discoveryContents = contentsRepository.findOnePerUrlType()
        return discoveryContents.map { ContentsDto.fromEntity(it) }
    }

    @Transactional
    fun createContents(contentsCreateRequest: ContentsCreateRequest): ContentsDto.Detail {
        val creator = userRepository.findById(contentsCreateRequest.creatorId)
            .orElseThrow { NotFoundException("Not found: User(${contentsCreateRequest.creatorId}") }

        val tagList = getOrCreateTagList(contentsCreateRequest.tagList)
        val bookList = bookRepository.findAllById(contentsCreateRequest.bookIsbnList)

        val contents = Contents(
            title = contentsCreateRequest.title,
            urls = contentsCreateRequest.urls.toMutableList(),
            image = contentsCreateRequest.image,
            description = contentsCreateRequest.description,
            memo = contentsCreateRequest.memo,
            creator = creator
        )

        tagList.forEach {
            contents.addTag(it)
        }

        bookList.forEach {
            contents.addBook(it)
        }

        val saved = contentsRepository.save(contents)
        return ContentsDto.Detail.fromEntity(saved)
    }

    @Transactional
    fun updateContents(id: Long, contentsUpdateRequest: ContentsUpdateRequest): ContentsDto.Detail {
        val contents = contentsRepository.findById(id).getOrNull()
            ?: throw NotFoundException("Not found contents(id: $id)")

        // 필드 업데이트 (null이 아닌 값만)
        contents.apply {
            contentsUpdateRequest.title?.let { title = it }
            contentsUpdateRequest.image?.let { image = it }
            contentsUpdateRequest.description?.let { description = it }
            contentsUpdateRequest.memo?.let { memo = it }
            contentsUpdateRequest.urls?.let { urls = it.toMutableList() }
        }

        // 태그 업데이트
        contentsUpdateRequest.tagList?.let { tagNames ->
            // 기존 태그 제거
            contents.tagContentsList.clear()
            val tagList = getOrCreateTagList(tagNames)
            tagList.forEach { tag -> contents.addTag(tag) }
        }

        // 책 목록 업데이트
        contentsUpdateRequest.bookIsbnList?.let { bookIsbnList ->
            val bookList = bookRepository.findAllById(bookIsbnList)
            contents.bookContentsList.clear()
            bookList.forEach { book -> contents.addBook(book) }
        }

        val savedContents = contentsRepository.save(contents)
        return ContentsDto.Detail.fromEntity(savedContents)
    }

    @Transactional
    fun deleteContents(id: Long) {
        val contents = contentsRepository.findById(id).getOrNull()
            ?: throw NotFoundException("Not found contents(id: $id)")

        // Soft delete
        contents.deletedAt = java.time.LocalDateTime.now()
        contentsRepository.save(contents)
    }

    private fun getOrCreateTagList(tagList: List<String>): List<Tag> {
        val newTagList = mutableListOf<Tag>()
        for (tagName in tagList) {
            var tag = tagRepository.findByName(tagName)
            if (tag == null) {
                tag = tagRepository.save(Tag(name = tagName))
            }
            newTagList.add(tag)
        }

        return newTagList
    }

}