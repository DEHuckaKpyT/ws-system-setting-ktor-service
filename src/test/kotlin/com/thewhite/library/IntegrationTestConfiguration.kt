package com.dehucka.library

import com.github.database.rider.core.configuration.DataSetConfig
import com.github.database.rider.core.connection.ConnectionHolderImpl
import com.github.database.rider.core.dataset.DataSetExecutorImpl
import com.zaxxer.hikari.HikariDataSource
import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.listeners.AfterProjectListener
import io.kotest.core.listeners.BeforeEachListener
import io.kotest.core.listeners.BeforeSpecListener
import io.kotest.core.spec.DslDrivenSpec
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.extensions.system.withSystemProperties
import io.ktor.server.testing.*
import org.apache.logging.log4j.kotlin.Logging
import org.testcontainers.containers.PostgreSQLContainer


/**
 * Created on 04.07.2023.
 *<p>
 *
 * @author Denis Matytsin
 */

val IntegrationTest by lazy { IntegrationTestExtension() }

class IntegrationTestConfiguration : AfterProjectListener, Logging {

    override suspend fun afterProject() {
        if (!started) return

        logger.info("Stopping TestApplication.")
        testApplication.stop()
        logger.info("TestApplication stopped.")

        dataSource.close()

        logger.info("Stopping TestContainer.")
        postgresContainer.stop()
        logger.info("TestContainer stopped.")
    }
}

class IntegrationTestExtension : BeforeSpecListener, BeforeEachListener, AfterEachListener, Logging {

    override suspend fun beforeSpec(spec: Spec) {
        if (started) return
        started = true

        logger.info("Starting TestContainer.")
        postgresContainer.start()
        logger.info("TestContainer started.")

        dataSetExecutor = DataSetExecutorImpl.instance(ConnectionHolderImpl(dataSource.connection))

        withSystemProperties(
            mapOf(
                "DATA_SOURCE_URL" to dataSource.connection.metaData.url,
                "DATA_SOURCE_USERNAME" to "test",
                "DATA_SOURCE_PASSWORD" to "test"
            )
        ) {
            logger.info("Starting TestApplication.")
            testApplication.start()
            logger.info("TestApplication started.")
        }
    }

    override suspend fun beforeEach(testCase: TestCase) {
        dataSetExecutor.clearDatabase(DataSetConfig())
    }

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        dataSetExecutor.clearDatabase(DataSetConfig())
    }
}

private var started = false

internal val postgresContainer by lazy {
    PostgreSQLContainer<Nothing>("postgres:14.5").apply {
        startupAttempts = 1
    }
}

val dataSource: HikariDataSource by lazy {
    postgresContainer.toDataSource {
        maximumPoolSize = 8
        idleTimeout = 10000
    }
}
internal lateinit var dataSetExecutor: DataSetExecutorImpl

val DslDrivenSpec.dataSource: HikariDataSource by lazy {
    dataSource
}

val DslDrivenSpec.dataSetExecutor: DataSetExecutorImpl by lazy {
    dataSetExecutor
}

fun DslDrivenSpec.dataSet(vararg datasets: String) {
    val dataSetConfig = DataSetConfig(*datasets)
    dataSetExecutor.createDataSet(dataSetConfig)
}

fun DslDrivenSpec.expectedDataSet(vararg datasets: String, ignoringColumns: Array<String> = emptyArray()) {
    val dataSetConfig = DataSetConfig(*datasets)
    dataSetExecutor.compareCurrentDataSetWith(dataSetConfig, ignoringColumns)
}

val DslDrivenSpec.testApplication: TestApplication by lazy {
    testApplication
}

private val testApplication by lazy { TestApplication {} }
