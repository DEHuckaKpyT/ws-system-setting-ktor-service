package com.thewhite.plugins

import com.thewhite.converters.SystemSettingConverter
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.mapstruct.factory.Mappers


/**
 * Created on 11.07.2023.
 *<p>
 *
 * @author Denis Matytsin
 */
fun Application.configureDependencyInjection() {
    install(Koin) {
        slf4jLogger()
        modules(
            defaultModule,
            convertersModule
        )
    }
}

val convertersModule = module {
    single { Mappers.getMapper(SystemSettingConverter::class.java) }
}