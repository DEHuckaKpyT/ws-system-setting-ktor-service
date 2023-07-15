package com.thewhite.plugins

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*


/**
 * Created on 14.01.2023.
 *<p>
 *
 * @author Denis Matytsin
 */
fun Application.enableShutdownUrl() {
    val shutdownUrl = environment.config.tryGetString("ktor.deployment.shutdown-url")
        ?: return

    install(ShutDownUrl.ApplicationCallPlugin) {
        shutDownUrl = shutdownUrl
        exitCodeSupplier = { 0 }
    }
}