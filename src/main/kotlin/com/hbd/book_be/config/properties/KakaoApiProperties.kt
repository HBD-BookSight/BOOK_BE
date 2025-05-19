package com.hbd.book_be.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "kakao")
class KakaoApiProperties {
    var restApiKey: String = ""
    var searchUrl: String = "https://dapi.kakao.com/v3/search/book"
}