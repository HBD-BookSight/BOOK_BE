package com.hbd.book_be.repository

import com.hbd.book_be.domain.Contents
import com.hbd.book_be.dto.request.ContentsSearchRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ContentsRepositoryCustom {
    fun findContentsWithConditions(searchRequest: ContentsSearchRequest, pageable: Pageable): Page<Contents>
}