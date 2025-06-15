package com.hbd.book_be.domain.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.hbd.book_be.domain.common.UrlInfo
import com.hbd.book_be.domain.converter.base.ListConverter
import jakarta.persistence.Converter

@Converter
class UrlInfoConverter : ListConverter<UrlInfo>() {
    override fun getTypeReference(): TypeReference<List<UrlInfo>> {
        return object : TypeReference<List<UrlInfo>>() {}
    }
}