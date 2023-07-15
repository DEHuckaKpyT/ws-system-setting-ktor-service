package com.dehucka.library

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.core.spec.DslDrivenSpec
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.internal.Diff
import net.javacrumbs.jsonunit.core.internal.Path
import org.testcontainers.containers.JdbcDatabaseContainer
import java.text.SimpleDateFormat


/**
 * Created on 03.07.2023.
 *<p>
 *
 * @author Denis Matytsin
 */
val mapper = ObjectMapper().apply {
    configure(SerializationFeature.INDENT_OUTPUT, true)
    setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
        indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
        indentObjectsWith(DefaultIndenter("  ", "\n"))
    })
    dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

    registerModule(JavaTimeModule())  // support java.time.* types

    registerModule(
        KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .configure(KotlinFeature.NullToEmptyCollection, false)
            .configure(KotlinFeature.NullToEmptyMap, false)
            .configure(KotlinFeature.NullIsSameAsDefault, false)
            .configure(KotlinFeature.SingletonSupport, false)
            .configure(KotlinFeature.StrictNullChecks, false)
            .build()
    )

//    registerModule(
//        SimpleModule()
//            .addSerializer(LocalDateTime::class.java, KLocalDateTimeSerializer())
//            .addDeserializer(LocalDateTime::class.java, KLocalDateTimeDeserializer())
//    )
}

fun DslDrivenSpec.client(): HttpClient {
    return testApplication.createClient {
        install(ContentNegotiation) {
            jackson(contentType = ContentType.Application.Json) { mapper }
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            header("Accept", "application/json, text/plain, */*")
        }
    }
}

inline fun <reified T> parse(path: String): T {
    val content = object {}.javaClass.getResource(path)?.readText() ?: error("Failed to get resource at $path")

    return mapper.readValue<T>(content)
}

inline fun <reified T> HttpRequestBuilder.setParsedBody(path: String) {
    setBody(parse<T>(path))
}

fun jsonLikeResource(path: String) = Matcher<String> { actual ->
    val expected = object {}.javaClass.getResource(path)?.readText() ?: error("Failed to get resource at $path")

    val diff: Diff = Diff.create(expected, actual, "fullJson", Path.root(), Configuration.empty())
    MatcherResult(
        diff.similar(),
        { diff.differences() },
        { "здесь лень что-то придумывать :(" },
    )
}

infix fun String.shouldBeJsonLikeResource(expected: String): String {
    this should jsonLikeResource(expected)
    return this
}

/**
 * Returns an initialized [HikariDataSource] connected to this [JdbcDatabaseContainer].
 *
 * @param configure a thunk to configure the [HikariConfig] used to create the datasource.
 */
fun JdbcDatabaseContainer<*>.toDataSource(configure: HikariConfig.() -> Unit = {}): HikariDataSource {
    val config = HikariConfig().apply { }
    config.jdbcUrl = jdbcUrl
    config.username = username
    config.password = password
    config.minimumIdle = 0
    config.configure()
    return HikariDataSource(config)
}
