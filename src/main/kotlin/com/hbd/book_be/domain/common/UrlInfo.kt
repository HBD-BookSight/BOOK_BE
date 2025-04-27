package com.hbd.book_be.domain.common

import com.hbd.book_be.domain.converter.base.ListConverter
import jakarta.persistence.Converter as _Converter

data class UrlInfo(
    val url: String,
    val type: String
) {

    @_Converter
    class Converter : ListConverter<UrlInfo>()
}

