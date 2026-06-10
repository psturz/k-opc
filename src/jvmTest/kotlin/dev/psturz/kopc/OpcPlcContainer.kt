package dev.psturz.kopc

import java.time.Duration
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

class OpcPlcContainer : GenericContainer<OpcPlcContainer>(DockerImageName.parse(IMAGE)) {
    init {
        withExposedPorts(PORT)
        portBindings = listOf("$PORT:$PORT")
        withCreateContainerCmdModifier { it.withHostName("localhost") }
        withCommand("--unsecuretransport", "--autoaccept", "--pn=$PORT")
        waitingFor(Wait.forLogMessage(".*OPC UA Server started.*\\n", 1))
        withStartupTimeout(Duration.ofMinutes(2))
    }

    val endpointUrl: String
        get() = "opc.tcp://localhost:$PORT"

    private companion object {
        const val IMAGE = "mcr.microsoft.com/iotedge/opc-plc:2.14.22"
        const val PORT = 50000
    }
}
