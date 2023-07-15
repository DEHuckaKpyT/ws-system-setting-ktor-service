package com.thewhite.converters

import com.thewhite.routes.dto.PutSystemSettingDto
import com.thewhite.services.arguments.PutSystemSettingArgument
import org.mapstruct.Mapper


/**
 * Created on 29.12.2022.
 *<p>
 *
 * @author Denis Matytsin
 */
@Mapper
interface SystemSettingConverter {

    fun toPutSystemSettingArgument(dto: PutSystemSettingDto): PutSystemSettingArgument

    fun toPutSystemSettingArgument(dto: Set<PutSystemSettingDto>): Set<PutSystemSettingArgument>
}