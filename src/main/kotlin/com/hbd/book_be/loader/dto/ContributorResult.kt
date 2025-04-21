package com.hbd.book_be.loader.dto

import com.hbd.book_be.domain.Author

data class ContributorResult(
    val authors: MutableList<Author>,
    val translators: MutableList<String>
)
