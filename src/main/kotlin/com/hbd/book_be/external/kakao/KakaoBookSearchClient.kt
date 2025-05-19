package com.hbd.book_be.external.kakao

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import com.hbd.book_be.config.properties.KakaoApiProperties

@Component
class KakaoBookSearchClient(
    private val restTemplate: RestTemplate,
    private val kakaoApiProperties: KakaoApiProperties
) {
    fun searchBook(request: KakaoApiRequest): KakaoApiResponse? {
        val url = UriComponentsBuilder.fromHttpUrl(kakaoApiProperties.searchUrl)
            .queryParam("query", request.query)
            .queryParam("sort", request.sort.name.lowercase())
            .queryParam("page", request.page)
            .queryParam("size", request.size)
            .queryParam("target", request.target)
            .build()
            .toUriString()

        val headers = HttpHeaders().apply {
            set("Authorization", "KakaoAK ${kakaoApiProperties.restApiKey}")
        }

        val entity = HttpEntity<Void>(headers)

        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            KakaoApiResponse::class.java
        )

        return response.body
    }
}