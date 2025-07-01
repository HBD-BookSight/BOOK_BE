package com.hbd.book_be.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
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
        val securitySchemeName = "bearerAuth"
        
        return OpenAPI()
            .info(
                Info()
                    .title("Book Service API")
                    .version("1.0")
                    .description("""
                        # Book Service API Documentation
                        
                        ## 인증 방법
                        
                        ### 1. JWT Bearer Token 인증
                        - **로그인**: `POST /api/v1/auth/login`으로 로그인 후 `accessToken` 획득
                        - **인증 헤더**: `Authorization: Bearer {accessToken}`
                        - **권한**: 수정/삭제는 ADMIN 권한 필요
                        
                        ### 2. 테스트 계정
                        - **일반 사용자**: testuser / password123
                        - **관리자**: admin / admin123
                        
                        ### 3. API 접근 권한
                        - **GET**: 모든 사용자 접근 가능
                        - **POST**: 모든 사용자 접근 가능
                        - **PUT/DELETE**: ADMIN 권한 필요
                    """.trimIndent())
            )
            .addSecurityItem(
                SecurityRequirement().addList(securitySchemeName)
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("""
                                JWT Bearer Token 인증
                                
                                1. `/api/v1/auth/login`으로 로그인
                                2. 응답에서 `accessToken` 획득
                                3. 아래 Authorize 버튼 클릭 후 `Bearer {accessToken}` 입력
                                
                                예시: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
                            """.trimIndent())
                    )
            )
    }
} 