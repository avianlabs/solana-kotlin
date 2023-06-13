package net.avianlabs.solana.client

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

public data class RpcRequest(
  private val id: Int? = null,
  private val invocation: RpcInvocation,
) {

  public fun buildBody(): JsonObject {
    val body: MutableMap<String, JsonElement> = mutableMapOf(
      "jsonrpc" to JsonPrimitive(HttpRequestExecutorConfig.version),
      "method" to JsonPrimitive(invocation.method)
    )
    body["params"] = invocation.params ?: JsonArray(emptyList())
    id?.let { body["id"] = JsonPrimitive(it) }
    return JsonObject(body)
  }
}
