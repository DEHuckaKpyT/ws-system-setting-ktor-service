package com.thewhite

import com.dehucka.library.UnitTestConfiguration
import com.thewhite.library.IntegrationTestConfiguration
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension


/**
 * Created on 04.07.2023.
 *<p>
 *
 * @author Denis Matytsin
 */
object KotestProjectConfig : AbstractProjectConfig() {

    override fun extensions(): List<Extension> {
        return listOf(IntegrationTestConfiguration(), UnitTestConfiguration())
    }
}