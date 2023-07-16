package com.thewhite.routes

import com.thewhite.library.IntegrationTest
import com.thewhite.library.client
import com.thewhite.library.dataSet
import com.thewhite.library.expectedDataSet
import com.thewhite.routes.dto.BatchPutSystemSettingDto
import com.thewhite.routes.dto.PutSystemSettingDto
import com.thewhite.routes.dto.ValueDto
import io.kotest.assertions.all
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK

/**
 * Created on 01.01.2023.
 *
 * @author Denis Matytsin
 */
@OptIn(ExperimentalKotest::class)
class SystemSettingRoutingIT : FreeSpec({

    extension(IntegrationTest)

    "get setting by key" {
        dataSet("datasets/routes/system-setting/get.json")

        // Act
        val actual = client().get("/system-setting/get") {
            parameter("key", "k1")
        }

        all(actual) {
            status shouldBe OK
            body<ValueDto<String>>() shouldBe ValueDto("v1")
        }
    }

    "get settings by keys" {
        dataSet("datasets/routes/system-setting/get.json")

        // Act
        val actual = client().get("/system-setting/get/batch") {
            parameter("keys", "k1")
            parameter("keys", "k2")
            parameter("keys", "k3")
        }

        all(actual) {
            status shouldBe OK
            body<Map<String, String?>>() shouldBe mapOf(
                Pair("k1", "v1"),
                Pair("k2", null),
                Pair("k3", null)
            )
        }
    }

    "put setting" - {
        "successfully" {
            // Act
            val actual = client().post("/system-setting/put") {
                setBody(PutSystemSettingDto("k1", "v1"))
            }

            all(actual) {
                status shouldBe OK
            }
            expectedDataSet("datasets/routes/system-setting/put__excepted.json")
        }

        "with empty params" {
            // Act
            val actual = client().post("/system-setting/put") {
                setBody(PutSystemSettingDto("", ""))
            }

            all(actual) {
                status shouldBe BadRequest
                body<Map<String, String>>() shouldBe mapOf("error" to ".key must be not blank\n.value must be not blank")
            }
        }
    }

    "put settings" - {
        "successfully" {
            val body = BatchPutSystemSettingDto(
                setOf(
                    PutSystemSettingDto("k1", "v1"),
                    PutSystemSettingDto("k2", null)
                )
            )

            // Act
            val actual = client().post("/system-setting/put/batch") {
                setBody(body)
            }

            all(actual) {
                status shouldBe OK
            }
            expectedDataSet("datasets/routes/system-setting/put-batch__excepted.json")
        }

        "with empty params" {
            // Act
            val actual = client().post("/system-setting/put/batch") {
                setBody(BatchPutSystemSettingDto(emptySet()))
                setBody(BatchPutSystemSettingDto(emptySet()))
            }

            all(actual) {
                status shouldBe BadRequest
                body<Map<String, String>>() shouldBe mapOf("error" to ".settings must be not empty")
            }
        }
    }
})