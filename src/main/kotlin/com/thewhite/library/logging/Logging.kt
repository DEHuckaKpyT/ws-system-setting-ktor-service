package com.thewhite.library.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * Created on 16.07.2023.
 *<p>
 *
 * @author Denis Matytsin
 */
interface Logging {
    @Suppress("unused")
    val log: Logger
        get() = LoggerFactory.getLogger(this.javaClass);
}