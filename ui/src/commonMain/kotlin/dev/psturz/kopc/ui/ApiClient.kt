package dev.psturz.kopc.ui

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class ApiClient(val baseUrl: String = "http://localhost:8080") {
    private val wsBaseUrl = baseUrl.replaceFirst("http", "ws")

    private val json = Json { ignoreUnknownKeys = true }

    private val client = HttpClient {
        expectSuccess = true
        install(ContentNegotiation) { json(json) }
        install(WebSockets) { contentConverter = KotlinxWebsocketSerializationConverter(json) }
    }

    /**
     * Turns a failed call into a message worth showing a user: unwraps the server's
     * `{"error": "..."}` body on HTTP errors, and gives a specific hint when no HTTP response
     * came back at all (the most common first-run mistake: forgetting to start `:server`).
     */
    suspend fun describeError(e: Throwable): String = when (e) {
        is ResponseException ->
            runCatching { e.response.body<ErrorResponse>().error }.getOrDefault(e.message ?: e.toString())
        else ->
            "Cannot reach k-opc server at $baseUrl. Is `:server:run` running? (${e.message ?: e::class.simpleName})"
    }

    suspend fun connect(request: ConnectRequest): ConnectResponse =
        client.post("$baseUrl/connect") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun disconnect(sessionId: String) {
        client.post("$baseUrl/disconnect/$sessionId")
    }

    suspend fun read(sessionId: String, nodeIds: List<String>): ReadResponse =
        client.post("$baseUrl/read/$sessionId") {
            contentType(ContentType.Application.Json)
            setBody(ReadRequest(nodeIds))
        }.body()

    suspend fun write(sessionId: String, nodeId: String, value: String) {
        client.post("$baseUrl/write/$sessionId") {
            contentType(ContentType.Application.Json)
            setBody(WriteRequest(nodeId, value))
        }
    }

    suspend fun browse(sessionId: String, nodeId: String): BrowseResponse =
        client.post("$baseUrl/browse/$sessionId") {
            contentType(ContentType.Application.Json)
            setBody(BrowseRequest(nodeId))
        }.body()

    fun monitor(sessionId: String, nodeIds: List<String>): Flow<ValueChangeDto> = callbackFlow {
        val job = launch {
            client.webSocket("$wsBaseUrl/monitor/$sessionId") {
                sendSerialized(MonitorRequest(nodeIds))
                while (true) {
                    trySend(receiveDeserialized<ValueChangeDto>())
                }
            }
        }
        awaitClose { job.cancel() }
    }

    fun close() = client.close()
}
