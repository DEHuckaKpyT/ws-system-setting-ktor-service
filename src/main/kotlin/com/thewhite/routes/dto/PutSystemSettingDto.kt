package com.thewhite.routes.dto

import com.papsign.ktor.openapigen.annotations.Request
import com.thewhite.library.throwIfNotValid
import io.konform.validation.Validation
import io.konform.validation.jsonschema.minLength


/**
 * Created on 29.12.2022.
 *<p>
 *
 * @author Denis Matytsin
 */
@Request("Настройка")
data class PutSystemSettingDto(
    val key: String,
    val value: String? = null
) {

    fun validate(): PutSystemSettingDto {
        Validation {
            PutSystemSettingDto::key {
                minLength(1) hint "must be not blank"
            }
            PutSystemSettingDto::value ifPresent {
                minLength(1) hint "must be not blank"
            }
        }.throwIfNotValid(this)

        return this
    }
}