package net.avianlabs.solana.client

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class RpcResponse(
  val id: Int,
  val jsonrpc: String,
  val result: JsonElement? = null,
  val error: RpcError? = null,
)

@Serializable
public data class RPC<T>(
  val context: Context?,
  val value: T? = null,
) {

  @Serializable
  public data class Context(
    val slot: Long,
    val apiVersion: String?,
  )
}
