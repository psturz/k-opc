package dev.psturz.kopc.server

import dev.psturz.kopc.NodeId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.util.getOrFail
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.collect

fun Application.configureRouting(sessions: OpcUaSessionManager) {
    routing {
        get("/health") {
            call.respondText("ok")
        }

        post("/connect") {
            val request = call.receive<ConnectRequest>()
            val sessionId = sessions.connect(request)
            call.respond(ConnectResponse(sessionId))
        }

        post("/disconnect/{sessionId}") {
            sessions.disconnect(call.parameters.getOrFail("sessionId"))
            call.respond(HttpStatusCode.NoContent)
        }

        post("/read/{sessionId}") {
            val client = sessions[call.parameters.getOrFail("sessionId")]
            val request = call.receive<ReadRequest>()
            val values = client.readValues(request.nodeIds.map { NodeId(it) })
            call.respond(ReadResponse(values.map { it.toDto() }))
        }

        post("/write/{sessionId}") {
            val client = sessions[call.parameters.getOrFail("sessionId")]
            val request = call.receive<WriteRequest>()
            val nodeId = NodeId(request.nodeId)
            val currentValue = client.readValue(nodeId).value
            client.writeValue(nodeId, coerceValue(request.value, currentValue))
            call.respond(HttpStatusCode.NoContent)
        }

        post("/browse/{sessionId}") {
            val client = sessions[call.parameters.getOrFail("sessionId")]
            val request = call.receive<BrowseRequest>()
            val nodes = client.browse(NodeId(request.nodeId))
            call.respond(BrowseResponse(nodes.map { it.toDto() }))
        }

        webSocket("/monitor/{sessionId}") {
            val client = sessions[call.parameters.getOrFail("sessionId")]
            val request = receiveDeserialized<MonitorRequest>()

            client.monitor(request.nodeIds.map { NodeId(it) }).collect { change ->
                sendSerialized(ValueChangeDto(change.nodeId.value, change.value.toDto()))
            }
        }
    }
}
