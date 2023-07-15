package com.thewhite.library

import com.thewhite.library.exceptions.BadArgumentException
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.konform.validation.Validation
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.system.measureTimeMillis


/**
 * Created on 15.07.2023.
 *<p>
 *
 * @author Denis Matytsin
 */
suspend fun ApplicationEngineEnvironmentBuilder.applyConfig(): ApplicationConfig {
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

suspend fun getRemoteConfigs(config: Config): Config = coroutineScope {
    var remoteConfig = ConfigFactory.empty()
    val configServiceUrl = config.getString("config-service.url")
    val appName = config.getString("ktor.application.name")
    val extensions = mutableListOf("properties", "yml", "yaml", "conf", "json")

    HttpClient(Apache).use { client ->
        extensions.map { ext ->
            async(Dispatchers.Default) {
                client.get("$configServiceUrl/$appName/default/main/$appName.$ext")
            }
        }.awaitAll().forEach {
            remoteConfig = remoteConfig.withFallback(ConfigFactory.parseString(it.bodyAsText()))
        }
    }

    remoteConfig
}

fun <T> Validation<T>.throwIfNotValid(value: T) {
    val errors = validate(value).errors
    if (errors.isEmpty()) return

    errors.joinToString(separator = "\n") {
        "${it.dataPath} ${it.message}"
    }.let {
        throw BadArgumentException(it)
    }
}