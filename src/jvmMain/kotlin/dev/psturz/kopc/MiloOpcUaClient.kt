package dev.psturz.kopc

import java.security.Security
import kotlinx.coroutines.future.await
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.eclipse.milo.opcua.sdk.client.OpcUaClient as MiloClient
import org.eclipse.milo.opcua.sdk.client.identity.AnonymousProvider
import org.eclipse.milo.opcua.sdk.client.identity.IdentityProvider
import org.eclipse.milo.opcua.sdk.client.identity.UsernameProvider
import org.eclipse.milo.opcua.stack.core.NodeIds
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator

class MiloOpcUaClient(
    private val endpointUrl: String,
    private val securityPolicy: SecurityPolicy = SecurityPolicy.None,
    private val username: String? = null,
    private val password: String? = null,
) : OpcUaClient {
    private var client: MiloClient? = null

    companion object {
        init {
            // The JDK's default JCE providers don't support the RSASSA-PSS signature
            // algorithm name Milo uses for SecurityPolicy.Aes256_Sha256_RsaPss.
            Security.addProvider(BouncyCastleProvider())
        }
    }

    override suspend fun connect() {
        val policyUri = securityPolicy.uri
        val identityProvider: IdentityProvider =
            if (username != null && password != null) UsernameProvider(username, password) else AnonymousProvider()

        client = MiloClient.create(
            endpointUrl,
            { endpoints: List<EndpointDescription> ->
                endpoints.stream()
                    .filter { e: EndpointDescription -> e.securityPolicyUri == policyUri }
                    .findFirst()
            },
            { },
            { config ->
                config.setIdentityProvider(identityProvider)
                if (securityPolicy != SecurityPolicy.None) {
                    val applicationUri = "urn:dev:psturz:kopc:client"
                    val keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048)
                    val certificate = SelfSignedCertificateBuilder(keyPair)
                        .setCommonName("k-opc client")
                        .setOrganization("psturz")
                        .setApplicationUri(applicationUri)
                        .build()
                    config.setApplicationUri(applicationUri)
                    config.setKeyPair(keyPair)
                    config.setCertificate(certificate)
                    config.setCertificateChain(arrayOf(certificate))
                }
            },
        ).also { it.connectAsync().await() }
    }

    override suspend fun disconnect() {
        client?.disconnectAsync()?.await()
        client = null
    }

    override suspend fun readValue(nodeId: String): OpcUaValue {
        val miloClient = requireNotNull(client) { "Client not connected" }
        val node = NodeId.parse(nodeId)
        val dataValue = miloClient.readValuesAsync(0.0, TimestampsToReturn.Both, listOf(node)).await().first()
        return dataValue.toOpcUaValue()
    }

    override suspend fun writeValue(nodeId: String, value: Any) {
        val miloClient = requireNotNull(client) { "Client not connected" }
        val node = NodeId.parse(nodeId)
        miloClient.writeValuesAsync(listOf(node), listOf(DataValue(Variant(value)))).await()
    }

    override suspend fun browse(nodeId: String): List<OpcUaNode> {
        val miloClient = requireNotNull(client) { "Client not connected" }
        val node = NodeId.parse(nodeId)
        val browseDescription = BrowseDescription(
            node,
            BrowseDirection.Forward,
            NodeIds.HierarchicalReferences,
            true,
            uint(0),
            uint(BrowseResultMask.All.value),
        )
        val browseResult = miloClient.browseAsync(browseDescription).await()
        return (browseResult.references ?: emptyArray()).map { ref ->
            OpcUaNode(
                nodeId = ref.nodeId.toParseableString(),
                browseName = ref.browseName?.name ?: "",
                displayName = ref.displayName?.text ?: "",
                nodeClass = ref.nodeClass?.name ?: "Unknown",
            )
        }
    }

    private fun DataValue.toOpcUaValue() = OpcUaValue(
        value = value.value,
        statusCode = statusCode.value,
        sourceTimestamp = sourceTime?.javaInstant?.toEpochMilli(),
        serverTimestamp = serverTime?.javaInstant?.toEpochMilli(),
    )
}
