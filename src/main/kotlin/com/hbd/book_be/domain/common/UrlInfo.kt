package com.hbd.book_be.domain.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class UrlInfo(
    @JsonProperty("url")
    val url: String,
    @JsonProperty("type")
    val type: String
)