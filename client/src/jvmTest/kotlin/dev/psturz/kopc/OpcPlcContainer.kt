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
        withCommand(
            "--unsecuretransport",
            "--autoaccept",
            "--trustowncert",
            "--pn=$PORT",
            "--du=$USERNAME",
            "--dc=$PASSWORD",
        )
        waitingFor(Wait.forLogMessage(".*OPC UA Server started.*\\n", 1))
        withStartupTimeout(Duration.ofMinutes(2))
    }

    val endpointUrl: String
        get() = "opc.tcp://localhost:$PORT"

    companion object {
        // The Basic256Sha256/Sign&Encrypt secured endpoint is exposed by default on the
        // same endpointUrl/port alongside the unsecured one added via --unsecuretransport.
        const val USERNAME = "user1"
        const val PASSWORD = "TestPassword123!"

        private const val IMAGE = "mcr.microsoft.com/iotedge/opc-plc:2.14.22"
        private const val PORT = 50000
    }
}
