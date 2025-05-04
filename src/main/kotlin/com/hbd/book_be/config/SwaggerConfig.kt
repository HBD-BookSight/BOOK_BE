package com.hbd.book_be.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun publicApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("web-api")
            .pathsToMatch("/api/**")
            .packagesToScan("com.hbd.book_be.controller.api")
            .build()
    }

    @Bean
    fun adminApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("admin-api")
            .pathsToMatch("/admin/**")
            .packagesToScan("com.hbd.book_be.controller.admin")
            .build()
    }

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Book Service API")
                    .version("1.0")
                    .description("Book Service API Documentation")
            )
    }
} 