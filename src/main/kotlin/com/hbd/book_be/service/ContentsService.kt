package com.hbd.book_be.service

import com.hbd.book_be.domain.Contents
import com.hbd.book_be.domain.Tag
import com.hbd.book_be.dto.ContentsDto
import com.hbd.book_be.dto.DiscoveryContentsDto
import com.hbd.book_be.dto.request.ContentsCreateRequest
import com.hbd.book_be.dto.request.ContentsSearchRequest
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class ContentsService(
    private val contentsRepository: ContentsRepository,
    private val discoveryContentsRepository: DiscoveryContentsRepository,
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository,
    private val bookRepository: BookRepository
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
        orderBy: String,
        direction: String,
        searchRequest: ContentsSearchRequest
    ): Page<ContentsDto> {
        val sortDirection = Sort.Direction.fromString(direction)
        val sort = Sort.by(sortDirection, orderBy)
        val pageRequest = PageRequest.of(page, limit, sort)

        val contentsPage = contentsRepository.findAllActiveWithConditions(searchRequest, pageRequest)
        return contentsPage.map { ContentsDto.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    fun getDiscoveryContents(
        limit: Int,
    ): List<DiscoveryContentsDto> {
        val pageRequest = PageRequest.of(0, limit)
        val discoveryContentsPage = discoveryContentsRepository.findRecentDiscoveryContents(pageRequest)
        
        return discoveryContentsPage.map { DiscoveryContentsDto.fromEntity(it) }
    }

    @Transactional
    fun createContents(contentsCreateRequest: ContentsCreateRequest): ContentsDto {
        val creator = userRepository.findById(contentsCreateRequest.creatorId)
            .orElseThrow { NotFoundException("Not found: User(${contentsCreateRequest.creatorId}") }

        val tagList = getOrCreateTagList(contentsCreateRequest.tagList)
        val bookList = bookRepository.findAllById(contentsCreateRequest.bookIsbnList)

        val contents = Contents(
            type = contentsCreateRequest.type,
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
        return ContentsDto.fromEntity(saved)
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