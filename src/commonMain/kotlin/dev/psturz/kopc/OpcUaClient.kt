package dev.psturz.kopc

interface OpcUaClient {
    suspend fun connect()
    suspend fun disconnect()
    suspend fun readValue(nodeId: String): OpcUaValue
    suspend fun writeValue(nodeId: String, value: Any)
    suspend fun browse(nodeId: String): List<OpcUaNode>
}
