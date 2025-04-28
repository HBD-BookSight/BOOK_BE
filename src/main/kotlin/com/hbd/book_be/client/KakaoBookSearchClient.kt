package com.hbd.book_be.client

import com.hbd.book_be.dto.request.KakaoBookRequest
import com.hbd.book_be.dto.response.KakaoBookResponse
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestTemplate

@Component
class KakaoBookSearchClient(
    private val restTemplate: RestTemplate,
    @Value("\${kakao.rest-api-key}") private val kakaoApiKey: String
) {
    fun searchBook(request: KakaoBookRequest): KakaoBookResponse? {
        val url = UriComponentsBuilder.fromHttpUrl("https://dapi.kakao.com/v3/search/book")
            .queryParam("query", request.query)
            .queryParam("sort", request.sort.name.lowercase())
            .queryParam("page", request.page)
            .queryParam("size", request.size)
            .queryParam("target", request.target)
            .build()
            .toUriString()

        val headers = HttpHeaders().apply {
            set("Authorization", "KakaoAK $kakaoApiKey")
        }

        val entity = HttpEntity<Void>(headers)

        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            KakaoBookResponse::class.java
        )

        return response.body
    }
}
