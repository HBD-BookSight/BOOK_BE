package com.hbd.book_be.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import io.sentry.spring.boot.jakarta.SentryAutoConfiguration

/**
 * Sentry 설정 클래스
 * application.yml의 sentry.enabled 설정에 따라 조건부로 활성화됩니다.
 * 
 * sentry.enabled=false일 때는 이 클래스가 로드되지 않아
 * Sentry 관련 모든 기능이 비활성화됩니다.
 */
@Configuration
@ConditionalOnProperty(
    prefix = "sentry",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false
)
@AutoConfigureBefore(SentryAutoConfiguration::class)
class SentryConfig {
    // 이 클래스가 로드되면 Sentry auto-configuration이 정상 동작
    // 로드되지 않으면 Sentry 기능 완전 비활성화
}
