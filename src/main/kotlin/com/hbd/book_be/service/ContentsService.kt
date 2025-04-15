package com.hbd.book_be.service

import com.hbd.book_be.dto.ContentsDetailedDto
import com.hbd.book_be.dto.ContentsDto
import com.hbd.book_be.dto.request.ContentsCreateRequest
import com.hbd.book_be.domain.Contents
import com.hbd.book_be.domain.BookContents
import com.hbd.book_be.exception.NotFoundException
import com.hbd.book_be.repository.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class ContentsService(
    @Autowired
    private val contentsRepository: ContentsRepository,
    private val discoveryContentsRepository: DiscoveryContentsRepository,
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository,
    private val bookRepository: BookRepository
) {

    fun getContentsDetail(id: Long): ContentsDetailedDto {
        val contents = contentsRepository.findById(id).getOrNull()
        if (contents == null || contents.deletedAt != null) {
            throw NotFoundException("Not found Cotents(isbn: $id)")
        }

        return ContentsDetailedDto.fromEntity(contents)
    }

    fun getContents(page: Int, limit: Int, orderBy: String, direction: String): Page<ContentsDto> {
        val sortDirection = Sort.Direction.fromString(direction)
        val sort = Sort.by(sortDirection, orderBy)
        val pageRequest = PageRequest.of(page, limit, sort)

        val bookPage = contentsRepository.findAllNonDeletedContents(pageRequest)
        return bookPage.map { ContentsDto.fromEntity(it) }
    }

    fun getDiscoveryContents(): List<ContentsDto> {
        val discoveryList = discoveryContentsRepository.findAll()
        return discoveryList.map { ContentsDto.fromEntity(it.contents) }
    }

    fun addContents(request: ContentsCreateRequest): ContentsDto {
        val creator = userRepository.findById(request.creatorId)
            .orElseThrow { IllegalArgumentException("Creator not found: ${request.creatorId}") }

        val contents = Contents(
            type = request.type,
            url = request.url,
            image = request.image,
            description = request.description,
            memo = request.memo,
            creator = creator
        )

        // 태그 연관관계 설정
        val tags = tagRepository.findAllById(request.tagIds)
        tags.forEach { contents.addTag(it) }

        // 책 연관관계 설정
        val books = bookRepository.findAllById(request.bookIds)
        books.forEach { book ->
            val bookContents = BookContents(
                book = book,
                contents = contents
            )
            contents.bookContentsList.add(bookContents)
            book.bookContentsList.add(bookContents)
        }

        val saved = contentsRepository.save(contents)
        return ContentsDto.fromEntity(saved)
    }
}