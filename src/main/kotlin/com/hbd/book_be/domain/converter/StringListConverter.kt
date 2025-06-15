package com.hbd.book_be.domain.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.hbd.book_be.domain.converter.base.ListConverter

class StringListConverter : ListConverter<String>() {
    override fun getTypeReference(): TypeReference<List<String>> {
        return object : TypeReference<List<String>>() {}
    }
}