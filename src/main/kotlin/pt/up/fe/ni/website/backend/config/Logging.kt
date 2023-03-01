package pt.up.fe.ni.website.backend.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger

interface Logging {
    val logger: Logger get() = getLogger(this::class.java)
}
