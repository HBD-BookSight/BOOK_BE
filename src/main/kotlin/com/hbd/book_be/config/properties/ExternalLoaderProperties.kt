package com.hbd.book_be.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "external.cultural-data-loader")
class ExternalLoaderProperties {
    var enabled: Boolean = false
    var batchSize: Int = 100
    var outputPath: String = "src/main/resources/output/books.json"
    var snapshotPath: String = "src/main/resources/output/enrichment_snapshot.json"
}
