package com.hbd.book_be.repository

import com.hbd.book_be.domain.Contents
import org.springframework.data.jpa.repository.JpaRepository

interface ContentsRepository : JpaRepository<Contents, Long>, ContentsRepositoryCustom