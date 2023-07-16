package com.thewhite.library

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.thewhite.library.exceptions.BadArgumentException
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.konform.validation.Validation
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import kotlin.system.measureTimeMillis


/**
 * Created on 15.07.2023.
 *<p>
 *
 * @author Denis Matytsin
 */
suspend fun ApplicationEngineEnvironmentBuilder.applyConfig(): ApplicationConfig {

    suspend fun getRemoteConfigs(config: Config): Config = coroutineScope {

        var remoteConfig = ConfigFactory.empty()
        val configServiceUrl = config.getString("config-service.url")
        val appName = config.getString("ktor.application.name")
        val profiles = config.tryGetString("ktor.application.profiles") ?: "default"

        HttpClient(Apache) {
            install(ContentNegotiation) {
                jackson(contentType = ContentType.Application.Json) { mapperConfig() }
            }
        }.use { client ->
            client.get("$configServiceUrl/$appName/$profiles").let {
                it.body<RemoteConfig>()
            }.let {
                log.info("Config profiles '${it.profiles}'")
                it.propertySources
            }.map {
                log.info("Parse Config from '${it.name}'")
                ConfigFactory.parseMap(it.source)
            }.forEach {
                remoteConfig = remoteConfig.withFallback(it)
            }
        }

        remoteConfig
    }

    var config = ConfigFactory.load()

    if (!config.getString("config-service.enabled").toBooleanStrict()) {
        return HoconApplicationConfig(config.resolve())
    }

    val remoteConfigsFetchingTime = measureTimeMillis {
        config = config.withFallback(getRemoteConfigs(config))
    }
    log.info("Configs fetched in $remoteConfigsFetchingTime ms")

    return HoconApplicationConfig(config.resolve())
}

data class RemoteConfig(
    val profiles: List<String>,
    val propertySources: List<RemoteConfigSource>
)

data class RemoteConfigSource(
    val name: String,
    val source: Map<String, String>
)

fun <T> Validation<T>.throwIfNotValid(value: T) {
    val errors = validate(value).errors
    if (errors.isEmpty()) return

    errors.joinToString(separator = "\n") {
        "${it.dataPath} ${it.message}"
    }.let {
        throw BadArgumentException(it)
    }
}

val mapperConfig: ObjectMapper.() -> Unit = {
    configure(SerializationFeature.INDENT_OUTPUT, true)
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
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
}

val mapper by lazy { ObjectMapper().apply { mapperConfig() } }