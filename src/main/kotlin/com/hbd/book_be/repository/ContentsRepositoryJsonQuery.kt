package com.hbd.book_be.repository

import com.hbd.book_be.domain.Contents

interface ContentsRepositoryJsonQuery {
    fun findOnePerUrlType(): List<Contents>
}
