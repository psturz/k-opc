package dev.psturz.kopc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val SECURITY_POLICIES = listOf("None", "Basic256Sha256", "Aes128_Sha256_RsaOaep", "Aes256_Sha256_RsaPss")

@Composable
fun App(api: ApiClient) {
    val scope = rememberCoroutineScope()

    var endpointUrl by remember { mutableStateOf("opc.tcp://localhost:50000") }
    var securityPolicy by remember { mutableStateOf(SECURITY_POLICIES[0]) }
    var securityPolicyMenuExpanded by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var sessionId by remember { mutableStateOf<String?>(null) }
    var connectionStatus by remember { mutableStateOf("Not connected") }

    var readNodeIds by remember { mutableStateOf("ns=0;i=2258") }
    var readResult by remember { mutableStateOf("") }

    var writeNodeId by remember { mutableStateOf("") }
    var writeValue by remember { mutableStateOf("") }
    var writeStatus by remember { mutableStateOf("") }

    var browseNodeId by remember { mutableStateOf("ns=0;i=85") }
    var browseResult by remember { mutableStateOf<List<OpcUaNodeDto>>(emptyList()) }
    var browseStatus by remember { mutableStateOf("") }

    var monitorNodeIds by remember { mutableStateOf("ns=0;i=2258") }
    var monitorLog by remember { mutableStateOf<List<String>>(emptyList()) }
    var monitorJob by remember { mutableStateOf<Job?>(null) }

    MaterialTheme {
        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(Modifier.padding(8.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Connect", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(endpointUrl, { endpointUrl = it }, label = { Text("Endpoint URL") }, modifier = Modifier.fillMaxWidth())

                    Box {
                        OutlinedTextField(
                            securityPolicy,
                            {},
                            readOnly = true,
                            label = { Text("Security Policy") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        DropdownMenu(securityPolicyMenuExpanded, { securityPolicyMenuExpanded = false }) {
                            SECURITY_POLICIES.forEach { policy ->
                                DropdownMenuItem(text = { Text(policy) }, onClick = {
                                    securityPolicy = policy
                                    securityPolicyMenuExpanded = false
                                })
                            }
                        }
                    }
                    Button({ securityPolicyMenuExpanded = true }) { Text("Choose policy: $securityPolicy") }

                    OutlinedTextField(username, { username = it }, label = { Text("Username (optional)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(password, { password = it }, label = { Text("Password (optional)") }, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(enabled = sessionId == null, onClick = {
                            scope.launch {
                                try {
                                    connectionStatus = "Connecting..."
                                    val response = api.connect(
                                        ConnectRequest(
                                            endpointUrl = endpointUrl,
                                            securityPolicy = securityPolicy,
                                            username = username.ifBlank { null },
                                            password = password.ifBlank { null },
                                        ),
                                    )
                                    sessionId = response.sessionId
                                    connectionStatus = "Connected (session ${response.sessionId})"
                                } catch (e: Exception) {
                                    connectionStatus = "Connect failed: ${api.describeError(e)}"
                                }
                            }
                        }) { Text("Connect") }

                        Button(enabled = sessionId != null, onClick = {
                            val id = sessionId ?: return@Button
                            scope.launch {
                                try {
                                    monitorJob?.cancel()
                                    monitorJob = null
                                    api.disconnect(id)
                                    sessionId = null
                                    connectionStatus = "Not connected"
                                } catch (e: Exception) {
                                    connectionStatus = "Disconnect failed: ${api.describeError(e)}"
                                }
                            }
                        }) { Text("Disconnect") }
                    }
                    Text(connectionStatus)
                }
            }

            Card(Modifier.padding(8.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Read", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(readNodeIds, { readNodeIds = it }, label = { Text("Node IDs (comma-separated)") }, modifier = Modifier.fillMaxWidth())
                    Button(enabled = sessionId != null, onClick = {
                        val id = sessionId ?: return@Button
                        scope.launch {
                            try {
                                val nodeIds = readNodeIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                val response = api.read(id, nodeIds)
                                readResult = response.values.joinToString("\n") { "${it.value} (status=${it.statusCode})" }
                            } catch (e: Exception) {
                                readResult = "Read failed: ${api.describeError(e)}"
                            }
                        }
                    }) { Text("Read") }
                    Text(readResult)
                }
            }

            Card(Modifier.padding(8.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Write", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(writeNodeId, { writeNodeId = it }, label = { Text("Node ID") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(writeValue, { writeValue = it }, label = { Text("Value") }, modifier = Modifier.fillMaxWidth())
                    Button(enabled = sessionId != null, onClick = {
                        val id = sessionId ?: return@Button
                        scope.launch {
                            try {
                                api.write(id, writeNodeId, writeValue)
                                writeStatus = "Write succeeded"
                            } catch (e: Exception) {
                                writeStatus = "Write failed: ${api.describeError(e)}"
                            }
                        }
                    }) { Text("Write") }
                    Text(writeStatus)
                }
            }

            Card(Modifier.padding(8.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Browse", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(browseNodeId, { browseNodeId = it }, label = { Text("Node ID") }, modifier = Modifier.fillMaxWidth())
                    Button(enabled = sessionId != null, onClick = {
                        val id = sessionId ?: return@Button
                        scope.launch {
                            try {
                                browseResult = api.browse(id, browseNodeId).nodes
                                browseStatus = ""
                            } catch (e: Exception) {
                                browseResult = emptyList()
                                browseStatus = "Browse failed: ${api.describeError(e)}"
                            }
                        }
                    }) { Text("Browse") }
                    Text(browseStatus)
                    browseResult.forEach { node ->
                        Text("${node.displayName} (${node.nodeId}) [${node.nodeClass}]")
                    }
                }
            }

            Card(Modifier.padding(8.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Monitor", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(monitorNodeIds, { monitorNodeIds = it }, label = { Text("Node IDs (comma-separated)") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(enabled = sessionId != null && monitorJob == null, onClick = {
                            val id = sessionId ?: return@Button
                            val nodeIds = monitorNodeIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            monitorLog = emptyList()
                            monitorJob = scope.launch {
                                try {
                                    api.monitor(id, nodeIds).collect { change ->
                                        monitorLog = (listOf("${change.nodeId}: ${change.value.value}") + monitorLog).take(20)
                                    }
                                } catch (e: Exception) {
                                    monitorLog = (listOf("Monitor stopped: ${api.describeError(e)}") + monitorLog).take(20)
                                }
                            }
                        }) { Text("Start monitoring") }

                        Button(enabled = monitorJob != null, onClick = {
                            monitorJob?.cancel()
                            monitorJob = null
                        }) { Text("Stop monitoring") }
                    }
                    Column(Modifier.fillMaxWidth()) {
                        monitorLog.forEach { line -> Text(line) }
                    }
                }
            }
        }
    }
}
