package net.tactware.nimbus.appwide.bl

import org.koin.core.annotation.Single

@Single([Logger::class])
class LoggerImpl : Logger {
   override fun d(tag: String, message: String) {
        println("DEBUG: $tag: $message")
    }

    override fun e(tag: String, message: String) {
        println("ERROR: $tag: $message")
    }

    override fun i(tag: String, message: String) {
        println("INFO: $tag: $message")
    }

    override fun w(tag: String, message: String) {
        println("WARN: $tag: $message")
    }

    override fun v(tag: String, message: String) {
        println("VERBOSE: $tag: $message")
    }
}