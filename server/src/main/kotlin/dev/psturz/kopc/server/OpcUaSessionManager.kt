package dev.psturz.kopc.server

import dev.psturz.kopc.MiloOpcUaClient
import dev.psturz.kopc.OpcUaClient
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy

class NoSuchSessionException(sessionId: String) : NoSuchElementException("Unknown session: $sessionId")

class OpcUaSessionManager {
    private val sessions = ConcurrentHashMap<String, OpcUaClient>()

    suspend fun connect(request: ConnectRequest): String {
        val client = MiloOpcUaClient(
            endpointUrl = request.endpointUrl,
            securityPolicy = SecurityPolicy.valueOf(request.securityPolicy),
            username = request.username,
            password = request.password,
        )
        client.connect()

        val sessionId = UUID.randomUUID().toString()
        sessions[sessionId] = client
        return sessionId
    }

    suspend fun disconnect(sessionId: String) {
        sessions.remove(sessionId)?.disconnect()
    }

    operator fun get(sessionId: String): OpcUaClient = sessions[sessionId] ?: throw NoSuchSessionException(sessionId)
}
