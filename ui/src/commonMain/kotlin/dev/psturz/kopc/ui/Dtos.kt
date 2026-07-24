package dev.psturz.kopc.ui

import kotlinx.serialization.Serializable

@Serializable
data class ConnectRequest(
    val endpointUrl: String,
    val securityPolicy: String = "None",
    val username: String? = null,
    val password: String? = null,
)

@Serializable
data class ConnectResponse(val sessionId: String)

@Serializable
data class OpcUaValueDto(
    val value: String?,
    val statusCode: Long,
    val sourceTimestamp: Long?,
    val serverTimestamp: Long?,
)

@Serializable
data class ReadRequest(val nodeIds: List<String>)

@Serializable
data class ReadResponse(val values: List<OpcUaValueDto>)

@Serializable
data class WriteRequest(val nodeId: String, val value: String)

@Serializable
data class BrowseRequest(val nodeId: String)

@Serializable
data class OpcUaNodeDto(
    val nodeId: String,
    val browseName: String,
    val displayName: String,
    val nodeClass: String,
)

@Serializable
data class BrowseResponse(val nodes: List<OpcUaNodeDto>)

@Serializable
data class MonitorRequest(val nodeIds: List<String>)

@Serializable
data class ValueChangeDto(val nodeId: String, val value: OpcUaValueDto)

@Serializable
data class ErrorResponse(val error: String)
