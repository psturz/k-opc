package dev.psturz.kopc

import io.kotest.core.config.AbstractProjectConfig

object ProjectConfig : AbstractProjectConfig() {
    val opcPlc = OpcPlcContainer()

    override suspend fun beforeProject() {
        opcPlc.start()
    }

    override suspend fun afterProject() {
        opcPlc.stop()
    }
}
