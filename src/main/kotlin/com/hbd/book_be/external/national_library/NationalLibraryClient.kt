package com.hbd.book_be.external.national_library

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.convertValue
import com.hbd.book_be.external.national_library.dto.NationalLibraryBookResponse
import com.hbd.book_be.external.national_library.dto.NationalLibrarySearchRequest
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException
import java.net.URI
import java.time.Duration

@Component
class NationalLibraryClient(
    @Value("\${national-library.url}")
    private val url: String,

    @Value("\${national-library.key}")
    private val key: String,

    ) {
    private val connectTimeoutMillis: Long = 3000L
    private val readTimeoutMillis: Long = 5000L
    private val retry = 3

    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        findAndRegisterModules()
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }

    fun search(
        request: NationalLibrarySearchRequest
    ): NationalLibraryBookResponse? {
        val restTemplate = buildRestTemplate()
        val uri = buildUri(request)

        return restTemplate.getForObject(uri, NationalLibraryBookResponse::class.java)
    }

    private fun buildRestTemplate(): RestTemplate {
        val connManager = PoolingHttpClientConnectionManager().apply {
            maxTotal = 100
            defaultMaxPerRoute = 20
        }
        val httpClient = HttpClients.custom()
            .setConnectionManager(connManager)
            .build()

        val requestFactory = HttpComponentsClientHttpRequestFactory(httpClient).apply {
            setConnectTimeout(Duration.ofMillis(connectTimeoutMillis))
            setReadTimeout(Duration.ofMillis(readTimeoutMillis))
        }

        val restTemplate = RestTemplateBuilder()
            .additionalInterceptors(clientHttpRequestInterceptor())
            .build()
        restTemplate.requestFactory = requestFactory

        return restTemplate
    }


    private fun clientHttpRequestInterceptor(): ClientHttpRequestInterceptor {
        return ClientHttpRequestInterceptor { request, body, execution ->
            val retryTemplate = RetryTemplate()
            retryTemplate.setRetryPolicy(SimpleRetryPolicy(retry))
            try {
                return@ClientHttpRequestInterceptor retryTemplate.execute<ClientHttpResponse, IOException> {
                    execution.execute(request, body)
                }
            } catch (t: Throwable) {
                throw RuntimeException(t)
            }
        }
    }

    private fun buildUri(request: NationalLibrarySearchRequest): URI {
        val uriBuilder = UriComponentsBuilder.fromUriString(url)
        val requestWithCertKey = request.copy(
            certKey = request.certKey ?: key
        )

        val requestMap: Map<String, Any?> = objectMapper.convertValue(requestWithCertKey)
        requestMap.forEach { (name, value) ->
            if (value != null) {
                uriBuilder.queryParam(name, value.toString())
            }
        }

        return uriBuilder.build().encode().toUri()
    }

}
