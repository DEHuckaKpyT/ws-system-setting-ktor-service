import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlin_version: String by project
val ktor_version: String by project
val koin_version: String by project
val kotest_version: String by project
val logback_version: String by project
val exposed_version: String by project
val hikaricp_version: String by project
val postgresql_version: String by project
val eureka_version: String by project
val openapigenerator_version: String by project
val testcontainers_version: String by project
val kmapper_version: String by project

plugins {
    application
    kotlin("jvm") version "1.8.22"
    kotlin("kapt") version "1.8.22"
    id("io.ktor.plugin") version "2.2.3"
    id("com.google.devtools.ksp") version "1.8.22-1.0.11"
}

group = "com.thewhite"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    mavenLocal()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    //region ktor
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-jackson-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-apache-jvm:$ktor_version")
    //endregion ktor

    //region koin
    compileOnly("io.insert-koin:koin-core-coroutines:$koin_version")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
    compileOnly("io.insert-koin:koin-annotations:1.2.2")
    ksp("io.insert-koin:koin-ksp-compiler:1.2.2")
    //endregion koin

    //region database
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("org.postgresql:postgresql:$postgresql_version")
    implementation("com.zaxxer:HikariCP:$hikaricp_version")
    //endregion database

    //region mapstruct
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    kapt("org.mapstruct:mapstruct-processor:1.5.5.Final")
    //endregion mapstruct

    //region logs
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.slf4j:slf4j-log4j12:2.0.6")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.2.0")
    //endregion logs

    //region other
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.netflix.eureka:eureka-client:$eureka_version")
    implementation("dev.forst:ktor-openapi-generator:$openapigenerator_version")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
    implementation("io.konform:konform-jvm:0.4.0")
    //endregion other

    //test

    //region kotest
    testImplementation("io.kotest:kotest-runner-junit5:$kotest_version")
    testImplementation("io.kotest:kotest-assertions-core:$kotest_version")
    testImplementation("io.kotest:kotest-property-jvm:$kotest_version")
    testImplementation("io.kotest:kotest-assertions-json-jvm:$kotest_version")
    testImplementation("io.kotest.extensions:kotest-assertions-ktor:1.0.3")
    //endregion kotest

    //region other
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    testImplementation("com.github.database-rider:rider-core:1.38.1")
    testImplementation("org.testcontainers:testcontainers:$testcontainers_version")
    testImplementation("org.testcontainers:postgresql:$testcontainers_version")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.38.0")
    testImplementation("net.javacrumbs.json-unit:json-unit:2.38.0")
    testImplementation("net.javacrumbs.json-unit:json-unit-json-path:2.38.0")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("io.insert-koin:koin-test:$koin_version")
    //endregion other
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}