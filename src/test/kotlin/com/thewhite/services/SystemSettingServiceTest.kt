package com.thewhite.services

import com.dehucka.library.DBUnitTest
import com.thewhite.library.database.InsertOrUpdate
import com.thewhite.library.database.upsert
import com.thewhite.models.SystemSetting
import com.thewhite.models.SystemSettings
import com.thewhite.services.arguments.PutSystemSettingArgument
import io.kotest.assertions.all
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*

@OptIn(ExperimentalKotest::class)
class SystemSettingServiceTest : FreeSpec({
    extension(DBUnitTest)

    val service = SystemSettingService()
    mockkObject(SystemSetting)
    mockkStatic("com.thewhite.library.database.DatabaseExtKt")
    val upserted = mockk<InsertOrUpdate<Number>>()

    "put key and value" {
        val argument = PutSystemSettingArgument("key", "value")
        coEvery { SystemSettings.upsert(body = any()) } returns upserted

        // Act
        service.put(argument)

        coVerify {
            SystemSettings.upsert(body = any())
        }
    }

    "get by key" {
        coEvery { SystemSetting["k1"].value } returns "v1"

        // Act
        val actual = service.getValue("k1")

        all {
            actual shouldBe "v1"
        }
        coVerify {
            SystemSetting["k1"].value
        }
    }
})
