package com.thewhite.plugins

import com.thewhite.library.exceptions.BadArgumentException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.jetbrains.exposed.dao.exceptions.EntityNotFoundException


/**
 * Created on 30.12.2022.
 *<p>
 *
 * @author Denis Matytsin
 */
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is BadArgumentException -> printAndRespond(call, cause, HttpStatusCode.BadRequest)
                is BadRequestException -> printAndRespond(call, cause, HttpStatusCode.BadRequest)
                is EntityNotFoundException -> printAndRespond(call, cause, HttpStatusCode.NotFound)
                else -> printAndRespond(call, cause, HttpStatusCode.InternalServerError)
            }
        }

        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(status, mapOf("error" to "Not Found"))
        }
    }
}

suspend fun printAndRespond(call: ApplicationCall, cause: Throwable, status: HttpStatusCode) {
    logError(call, cause)
    call.respond(status, mapOf("error" to cause.localizedMessage))
}