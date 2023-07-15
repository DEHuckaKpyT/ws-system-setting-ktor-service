package com.thewhite.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*


/**
 * Created on 27.01.2023.
 *<p>
 *
 * @author Denis Matytsin
 */
fun Application.configureCORS() {
    install(CORS) {
        anyHost()
        allowHeader("Content-Type")
        allowHeaders {
            true
        }
        allowOrigins {
            true
        }
    }
}