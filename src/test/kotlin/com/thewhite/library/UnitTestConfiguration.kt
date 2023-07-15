package com.dehucka.library

import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.listeners.BeforeEachListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.mockk.clearAllMocks


/**
 * Created on 12.07.2023.
 *<p>
 *
 * @author Denis Matytsin
 */
class UnitTestConfiguration : AfterEachListener {

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        clearAllMocks()
    }
}

val DBUnitTest by lazy { DBUnitTestExtension() }

class DBUnitTestExtension : BeforeEachListener, AfterEachListener {

//    init {
//        mockkStatic("com.dehucka.plugins.DatabaseConnectionKt")
//    }

    override suspend fun beforeEach(testCase: TestCase) {
        mockDatabase()
    }

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        unmockDatabase()
    }
}