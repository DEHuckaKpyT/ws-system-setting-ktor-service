package com.thewhite.services

import com.thewhite.library.database.batchUpsert
import com.thewhite.library.database.execute
import com.thewhite.library.database.read
import com.thewhite.library.database.upsert
import com.thewhite.models.SystemSetting
import com.thewhite.models.SystemSettings
import com.thewhite.models.SystemSettings.key
import com.thewhite.models.SystemSettings.value
import com.thewhite.services.arguments.PutSystemSettingArgument
import org.jetbrains.exposed.sql.select
import org.koin.core.annotation.Single
import java.time.LocalDateTime.now


/**
 * Created on 29.12.2022.
 *<p>
 *
 * @author Denis Matytsin
 */
@Single
class SystemSettingService {

    suspend fun put(argument: PutSystemSettingArgument) = execute {
        SystemSettings.upsert {
            it[key] = argument.key
            it[value] = argument.value
        }
    }

    suspend fun putAll(settings: Set<PutSystemSettingArgument>) = execute {
        SystemSettings.batchUpsert(settings) { batch, (k, v) ->
            batch[key] = k
            batch[value] = v
            batch[updatedDate] = now()
        }
    }

    suspend fun getValue(key: String): String? = read {
        SystemSetting[key].value
    }

    suspend fun getValues(keys: Set<String>): Map<String, String?> = read {
        SystemSettings.slice(key, value).select() {
            key inList keys
        }.associateTo(HashMap()) {
            it[key] to it[value]
        }.apply {
            keys.forEach { key ->
                if (key !in this.keys) {
                    this[key] = null
                }
            }
        }
    }
}