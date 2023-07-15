package com.thewhite.library.eureka

import com.thewhite.library.eureka.config.EurekaConfig
import com.netflix.appinfo.ApplicationInfoManager
import com.netflix.appinfo.InstanceInfo
import com.netflix.appinfo.PropertiesInstanceConfig
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider
import com.netflix.discovery.DefaultEurekaClientConfig
import com.netflix.discovery.DiscoveryClient
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.config.*


/**
 * Created on 15.07.2023.
 *<p>
 *
 * @author Denis Matytsin
 */
val EurekaClient = createApplicationPlugin(name = "eureka-client", "eureka", { EurekaConfig(it) }) {
    val enabled = applicationConfig?.tryGetString("eureka.enabled")?.toBooleanStrict() ?: true
    if (!enabled) return@createApplicationPlugin

    application.log.info("Starting eureka-client")

    val instanceConfig = CustomInstanceConfig(pluginConfig)
    val instanceInfo = EurekaConfigBasedInstanceInfoProvider(instanceConfig).get()
    val eurekaClientConfig = CustomEurekaClientConfig(pluginConfig)
    val applicationInfoManager = ApplicationInfoManager(instanceConfig, instanceInfo)

    val eurekaClient = DiscoveryClient(applicationInfoManager, eurekaClientConfig)

    fun startEureka() {
        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.UP)
    }

    fun stopEureka() {
        application.log.info("Stopping eureka-client")
        eurekaClient.shutdown()
    }

    on(MonitoringEvent(ApplicationStarted)) { application ->
        startEureka()
        application.log.info("Eureka-client is started")
    }

    on(MonitoringEvent(ApplicationStopped)) { application ->
        stopEureka()
        application.log.info("Eureka-client is stopped")

        // Release resources and unsubscribe from events
        application.environment.monitor.unsubscribe(ApplicationStarted) {}
        application.environment.monitor.unsubscribe(ApplicationStopped) {}
    }
}

class CustomInstanceConfig(private val pluginConfig: EurekaConfig) : PropertiesInstanceConfig() {
    override fun getAppname(): String {
        return pluginConfig.client.applicationName
    }

    override fun getHostName(refresh: Boolean): String {
        return pluginConfig.client.hostname
    }

    override fun getNonSecurePort(): Int {
        return pluginConfig.client.nonSecurePort
    }

    override fun getVirtualHostName(): String {
        return pluginConfig.client.applicationName
    }
}

class CustomEurekaClientConfig(private val pluginConfig: EurekaConfig) : DefaultEurekaClientConfig() {
    override fun getEurekaServerServiceUrls(myZone: String?): MutableList<String> {
        return mutableListOf(pluginConfig.server.serviceUrl)
    }

    override fun getInitialInstanceInfoReplicationIntervalSeconds(): Int {
        return pluginConfig.server.initialInstanceInfoReplicationIntervalSeconds
            ?: super.getInitialInstanceInfoReplicationIntervalSeconds()
    }

    override fun getRegistryFetchIntervalSeconds(): Int {
        return pluginConfig.server.registryFetchIntervalSeconds
            ?: super.getRegistryFetchIntervalSeconds()
    }
}
