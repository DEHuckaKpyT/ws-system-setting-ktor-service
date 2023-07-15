package com.thewhite.routes.dto

import com.papsign.ktor.openapigen.annotations.parameters.QueryParam


/**
 * Created on 18.01.2023.
 *<p>
 *
 * @author Denis Matytsin
 */
data class GetBatchSystemSettingDto(
    @QueryParam("Коллекция ключей") val keys: List<String>
)
