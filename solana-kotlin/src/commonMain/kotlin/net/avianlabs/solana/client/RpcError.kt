package net.avianlabs.solana.client

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
public data class RpcError(
  val message: String,
  val code: Int,
  val data: JsonObject? = null,
)
