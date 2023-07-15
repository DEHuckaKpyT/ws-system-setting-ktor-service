package com.thewhite.library.eureka.config

import io.ktor.server.config.*


/**
 * Created on 15.07.2023.
 *<p>
 *
 * @author Denis Matytsin
 */
data class EurekaConfig(private val config: ApplicationConfig) {

    val client = EurekaClientConfig(
        config.property("client.application-name").getString(),
        config.property("client.hostname").getString(),
        config.property("client.non-secure-port").getString().toInt()
    )

    val server = EurekaServerConfig(
        config.property("server.service-url").getString(),
        config.propertyOrNull("server.initial-instance-info-replication-interval-seconds")?.getString()?.toInt(),
        config.propertyOrNull("server.registry-fetch-interval-seconds")?.getString()?.toInt()
    )

    inline fun client(block: EurekaClientConfig.() -> Unit) {
        client.apply(block)
    }

    inline fun server(block: EurekaServerConfig.() -> Unit) {
        server.apply(block)
    }
}

data class EurekaClientConfig(
    var applicationName: String,
    var hostname: String,
    var nonSecurePort: Int
)

data class EurekaServerConfig(
    var serviceUrl: String,
    var initialInstanceInfoReplicationIntervalSeconds: Int? = null,
    var registryFetchIntervalSeconds: Int? = null
)