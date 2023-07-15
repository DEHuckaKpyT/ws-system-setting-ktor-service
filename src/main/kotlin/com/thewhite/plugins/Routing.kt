package com.thewhite.plugins

import com.thewhite.routes.systemSettingRouting
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        systemSettingRouting()
    }
}