package dev.psturz.kopc

import kotlinx.coroutines.flow.Flow

interface OpcUaClient {
    suspend fun connect()
    suspend fun disconnect()
    suspend fun readValue(nodeId: NodeId): OpcUaValue
    suspend fun readValues(nodeIds: List<NodeId>): List<OpcUaValue>
    suspend fun writeValue(nodeId: NodeId, value: Any)
    suspend fun browse(nodeId: NodeId): List<OpcUaNode>
    fun monitor(nodeIds: List<NodeId>): Flow<OpcUaValueChange>
}
