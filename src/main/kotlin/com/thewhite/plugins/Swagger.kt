package com.thewhite.plugins

import com.papsign.ktor.openapigen.OpenAPIGen
import com.papsign.ktor.openapigen.openAPIGen
import com.papsign.ktor.openapigen.route.apiRouting
import com.papsign.ktor.openapigen.route.route
import com.papsign.ktor.openapigen.schema.namer.DefaultSchemaNamer
import com.papsign.ktor.openapigen.schema.namer.SchemaNamer
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.reflect.KType

fun Application.configureSwagger() {
    // install OpenAPI plugin
    install(OpenAPIGen) {
        // this servers OpenAPI definition on /openapi.json
        serveOpenApiJson = true
        // this servers Swagger UI on /swagger-ui/index.html
        serveSwaggerUi = true
        info {
//            version = "0.0.1"
            title = this@configureSwagger.environment.config.property("ktor.application.name").getString()
//            description = "The Test API"
//            contact {
//                name = "Support"
//                email = "support@test.com"
//            }
        }
        // describe the server, add as many as you want
        server("http://localhost:8080/") {
            description = "cobrowsing-service"
        }
    }
    routing {
        get("/openapi.json") {
            call.respond(application.openAPIGen.api.serialize())
        }
        get("/swagger") {
            call.respondRedirect("/swagger-ui/index.html?url=/openapi.json", true)
        }
    }
}
