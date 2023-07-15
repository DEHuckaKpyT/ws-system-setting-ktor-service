package com.thewhite.routes

import com.papsign.ktor.openapigen.route.apiRouting
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import com.thewhite.converters.SystemSettingConverter
import com.thewhite.routes.dto.*
import com.thewhite.services.SystemSettingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject


/**
 * Created on 29.12.2022.
 *<p>
 *
 * @author Denis Matytsin
 */
fun Routing.systemSettingRouting() = apiRouting {

    val systemSettingService by inject<SystemSettingService>()
    val systemSettingConverter by inject<SystemSettingConverter>()

    route("/system-setting") {
        route("/get").get<GetSystemSettingDto, ValueDto<String>>(
            info("Получить значение по ключу")
        ) { params ->
            respond(ValueDto(systemSettingService.getValue(params.key)))
        }

        route("/get/batch").get<GetBatchSystemSettingDto, Map<String, String?>>(
            info("Получить значения пачки ключей")
        ) { params ->
            respond(systemSettingService.getValues(params.keys.toSet()))
        }

        route("/put").post<Unit, Unit, PutSystemSettingDto>(
            info("Сохранить/обновить ключ")
        ) { _, body ->
            systemSettingConverter.toPutSystemSettingArgument(body.validate())
                .let { systemSettingService.put(it) }
                .let { this@post.pipeline.call.response.status(HttpStatusCode.OK) }
        }

        route("/put/batch").post<Unit, Unit, BatchPutSystemSettingDto>(
            info("Сохранить/обновить пачку ключей")
        ) { _, body ->
            systemSettingConverter.toPutSystemSettingArgument(body.validate().settings)
                .let { systemSettingService.putAll(it) }
                .let { this@post.pipeline.call.response.status(HttpStatusCode.OK) }
        }
    }
}