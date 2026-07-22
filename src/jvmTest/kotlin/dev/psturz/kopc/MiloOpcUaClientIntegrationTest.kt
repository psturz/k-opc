package dev.psturz.kopc

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy

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

    test("reads a value over an explicit SecurityPolicy.None, anonymous connection") {
        client = MiloOpcUaClient(
            opcPlc.endpointUrl,
        )
        client.connect()

        val value = client.readValue("ns=0;i=2258")

        value.value.shouldNotBeNull()
    }

    withData(
        nameFn = { policy: SecurityPolicy -> "$policy secured, username/password authenticated connection" },
        SecurityPolicy.Basic256Sha256,
        SecurityPolicy.Aes128_Sha256_RsaOaep,
        SecurityPolicy.Aes256_Sha256_RsaPss,
    ) { policy ->
        test("reads a value") {
            client = MiloOpcUaClient(
                opcPlc.endpointUrl,
                securityPolicy = policy,
                username = OpcPlcContainer.USERNAME,
                password = OpcPlcContainer.PASSWORD,
            )
            client.connect()

            val value = client.readValue("ns=0;i=2258")

            value.value.shouldNotBeNull()
        }
    }
})
