package dev.psturz.kopc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull

class MiloOpcUaClientIntegrationTest : FunSpec({
    val opcPlc = ProjectConfig.opcPlc
    lateinit var client: MiloOpcUaClient

    beforeTest { client = MiloOpcUaClient(opcPlc.endpointUrl) }
    afterTest { client.disconnect() }

    test("reads the server current time after connecting") {
        client.connect()

        val value = client.readValue("ns=0;i=2258")

        value.value.shouldNotBeNull()
    }

    test("browses the objects folder") {
        client.connect()

        val nodes = client.browse("ns=0;i=85")

        nodes.shouldNotBeEmpty()
    }
})
