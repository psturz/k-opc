package dev.psturz.kopc

data class OpcUaValue(
    val value: Any?,
    val statusCode: Long,
    val sourceTimestamp: Long?,
    val serverTimestamp: Long?,
)

data class OpcUaNode(
    val nodeId: String,
    val browseName: String,
    val displayName: String,
    val nodeClass: String,
)

class OpcUaException(message: String, cause: Throwable? = null) : Exception(message, cause)
