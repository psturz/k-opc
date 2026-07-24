package dev.psturz.kopc

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy
import kotlin.time.Duration.Companion.milliseconds

class MiloOpcUaClientIntegrationTest : FunSpec({
    val opcPlc = ProjectConfig.opcPlc
    lateinit var client: MiloOpcUaClient

    beforeTest { client = MiloOpcUaClient(opcPlc.endpointUrl) }
    afterTest { client.disconnect() }

    test("reads the server current time after connecting") {
        client.connect()

        val value = client.readValue(NodeId("ns=0;i=2258"))

        value.value.shouldNotBeNull()
    }

    test("browses the objects folder") {
        client.connect()

        val nodes = client.browse(NodeId("ns=0;i=85"))

        nodes.shouldNotBeEmpty()
    }

    test("reads multiple values in a single batched request") {
        client.connect()

        val values = client.readValues(listOf(NodeId("ns=0;i=2258"), NodeId("ns=0;i=2255")))

        values shouldHaveSize 2
        values.forEach { it.value.shouldNotBeNull() }
    }

    test("writes a value and reads it back") {
        client.connect()
        val nodeId = NodeId("ns=3;s=FastNumberOfUpdates")

        client.writeValue(nodeId, 7)

        client.readValue(nodeId).value shouldBe 7
    }

    test("receives a value change while monitoring the server current time") {
        client.connect()

        val change = withTimeout(5_000.milliseconds) {
            client.monitor(listOf(NodeId("ns=0;i=2258"))).first()
        }

        change.nodeId shouldBe NodeId("ns=0;i=2258")
        change.value.value.shouldNotBeNull()
    }

    test("reads a value over an explicit SecurityPolicy.None, anonymous connection") {
        client = MiloOpcUaClient(
            opcPlc.endpointUrl,
        )
        client.connect()

        val value = client.readValue(NodeId("ns=0;i=2258"))

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

            val value = client.readValue(NodeId("ns=0;i=2258"))

            value.value.shouldNotBeNull()
        }
    }
})
