package com.hbd.book_be.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "spring.external.loader")
class ExternalLoaderProperties {
    var enabled: Boolean = false
    var batchSize: Int = 10000
}