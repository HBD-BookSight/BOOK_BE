package com.hbd.book_be.domain.converter

import com.hbd.book_be.domain.common.UrlInfo
import com.hbd.book_be.domain.converter.base.ListConverter
import jakarta.persistence.Converter

@Converter
class UrlInfoConverter : ListConverter<UrlInfo>()