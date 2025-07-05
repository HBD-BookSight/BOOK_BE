plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"

    // QueryDSL
    id("com.ewerk.gradle.plugins.querydsl") version "1.0.10"

    // JPA Entity에 no-arg 생성자 추가
    kotlin("plugin.noarg") version "1.9.25"
    kotlin("kapt") version "1.9.25"

    // Sentry
    id("io.sentry.jvm.gradle") version "5.8.0"
}

group = "com.hbd"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // httpClient
    implementation("org.apache.httpcomponents.client5:httpclient5")

    // spring-retry
    implementation("org.springframework.retry:spring-retry:2.0.11")

    // Springdoc OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("org.springdoc:springdoc-openapi-starter-common:2.6.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // DB
    runtimeOnly("com.mysql:mysql-connector-j")
    implementation("com.oracle.database.jdbc:ojdbc11:23.3.0.23.09")
    testRuntimeOnly("com.h2database:h2")

    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")

    // csv loader
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.15.2")

    // Sentry
    implementation("io.sentry:sentry-spring-boot-starter-jakarta:8.14.0")
    implementation("io.sentry:sentry-logback:8.14.0")

    // test
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.batch:spring-batch-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// QueryDSL 설정
val querydslDir = layout.buildDirectory.dir("generated/querydsl")

kapt {
    arguments {
        arg("querydsl.entityAccessors", "true")
        arg("querydsl.useFields", "true")
    }
}

sourceSets["main"].java {
    srcDir(querydslDir)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

// JPA Entity용 no-arg 생성자 자동 추가
noArg {
    annotation("jakarta.persistence.Entity")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Sentry Configuration
sentry {
    includeSourceContext.set(true)
    org.set("book-be-org")
    projectName.set("book-be")
    authToken.set(System.getenv("SENTRY_AUTH_TOKEN"))
    includeProguardMapping.set(false)
    telemetry.set(false)
}

// Sentry와 QueryDSL 간의 태스크 의존성 해결 및 조건부 비활성화
afterEvaluate {
    // application.yml에서 sentry.enabled 설정 확인
    val applicationYmlFile = file("src/main/resources/application.yml")
    var sentryEnabled = false
    
    if (applicationYmlFile.exists()) {
        val content = applicationYmlFile.readText()
        // 기본 sentry.enabled 값 찾기
        val enabledMatch = Regex("sentry:\\s*\\n\\s*enabled:\\s*(true|false)").find(content)
        if (enabledMatch != null) {
            sentryEnabled = enabledMatch.groupValues[1] == "true"
        }
        
        // 현재 활성 프로파일 확인
        val activeProfile = System.getProperty("spring.profiles.active") ?: "local"
        val profilePattern = "---[\\s\\S]*?on-profile:\\s*$activeProfile[\\s\\S]*?(?=---|\\z)"
        val profileMatch = Regex(profilePattern).find(content)
        
        if (profileMatch != null) {
            val profileSection = profileMatch.value
            val profileEnabledMatch = Regex("sentry:[\\s\\S]*?enabled:\\s*(true|false)").find(profileSection)
            if (profileEnabledMatch != null) {
                sentryEnabled = profileEnabledMatch.groupValues[1] == "true"
            }
        }
    }
    
    println("[BUILD] Sentry enabled: $sentryEnabled (profile: ${System.getProperty("spring.profiles.active") ?: "local"})")
    
    if (!sentryEnabled) {
        // Sentry 기능이 비활성화된 경우 빌드 태스크들도 비활성화
        tasks.findByName("sentryBundleSourcesJava")?.enabled = false
        tasks.findByName("sentryCollectSourcesJava")?.enabled = false
        tasks.findByName("generateSentryBundleIdJava")?.enabled = false
        println("[BUILD] Sentry build tasks disabled")
    } else {
        // Sentry가 활성화된 경우 QueryDSL과의 의존성 설정
        println("[BUILD] Setting up Sentry task dependencies")
        tasks.findByName("generateSentryBundleIdJava")?.let { sentryTask ->
            tasks.findByName("compileQuerydsl")?.let { querydslTask ->
                sentryTask.dependsOn(querydslTask)
            }
        }
        
        tasks.findByName("sentryCollectSourcesJava")?.let { sentryCollectTask ->
            tasks.findByName("compileQuerydsl")?.let { querydslTask ->
                sentryCollectTask.dependsOn(querydslTask)
            }
        }
    }
}
