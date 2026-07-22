package dev.psturz.kopc

data class NodeId(val value: String) {
    init {
        require(FORMAT.matches(value)) { "Invalid OPC UA NodeId format: '$value'" }
    }

    override fun toString() = value

    private companion object {
        val FORMAT = Regex("""(ns=\d+;)?[isgb]=.+""")
    }
}

data class OpcUaValue(
    val value: Any?,
    val statusCode: Long,
    val sourceTimestamp: Long?,
    val serverTimestamp: Long?,
)

data class OpcUaNode(
    val nodeId: NodeId,
    val browseName: String,
    val displayName: String,
    val nodeClass: String,
)

data class OpcUaValueChange(
    val nodeId: NodeId,
    val value: OpcUaValue,
)

class OpcUaException(message: String, cause: Throwable? = null) : Exception(message, cause)
