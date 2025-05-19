package com.hbd.book_be.external.national_library

import com.hbd.book_be.external.national_library.dto.NationalLibrarySearchRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles

// 국립중앙도서관 client integration test
@ActiveProfiles("test")
class NationalLibraryClientTest {

    private val client = NationalLibraryClient(
        url = "https://www.nl.go.kr/seoji/SearchApi.do",
        key = System.getenv("NATIONAL_LIBRARY_API_KEY"),
    )

    @Test
    fun 출판일_기준_검색_테스트() {
        // given
        val request = NationalLibrarySearchRequest(
            startPublishDate = "20250401",
            endPublishDate = "20250401",
        )

        // when
        val response = client.search(request)

        // then
        assertThat(response).isNotNull
        assertThat(response!!.totalCount).isGreaterThanOrEqualTo(1)
    }
}