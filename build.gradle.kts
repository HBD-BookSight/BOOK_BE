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

// Sentry와 QueryDSL 간의 태스크 의존성 해결
afterEvaluate {
    tasks.findByName("generateSentryBundleIdJava")?.let { sentryTask ->
        tasks.findByName("compileQuerydsl")?.let { querydslTask ->
            sentryTask.dependsOn(querydslTask)
        }
    }
}
