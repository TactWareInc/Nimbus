package net.tactware.nimbus.appwide.bl

import org.koin.core.component.KoinComponent

object LoggerStatic : KoinComponent, Logger {
    private val logger: Logger = getKoin().get<Logger>()

    override fun d(tag: String, message: String) {
        logger.d(tag, message)
    }

    override fun e(tag: String, message: String) {
        logger.e(tag, message)
    }

    override fun i(tag: String, message: String) {
        logger.i(tag, message)
    }

    override fun w(tag: String, message: String) {
        logger.w(tag, message)
    }

    override fun v(tag: String, message: String) {
        logger.v(tag, message)
    }
}

