package com.hbd.book_be.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("HBD API Documentation")
                .description("HBD Backend API Documentation")
                .version("v1.0.0")
        )
        .addServersItem(
            Server().url("http://localhost:8080").description("Local Server URL")
        )
} 