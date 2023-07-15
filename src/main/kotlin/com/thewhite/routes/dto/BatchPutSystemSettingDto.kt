package com.thewhite.routes.dto

import com.papsign.ktor.openapigen.annotations.Request
import com.thewhite.library.throwIfNotValid
import io.konform.validation.Validation
import io.konform.validation.jsonschema.minItems


/**
 * Created on 30.12.2022.
 *<p>
 *
 * @author Denis Matytsin
 */
@Request("Коллекция настроек")
data class BatchPutSystemSettingDto(
    val settings: Set<PutSystemSettingDto>
) {

    fun validate(): BatchPutSystemSettingDto {
        Validation {
            BatchPutSystemSettingDto::settings {
                minItems(1) hint "must be not empty"
            }
        }.throwIfNotValid(this)

        settings.forEach { it.validate() }

        return this
    }
}

