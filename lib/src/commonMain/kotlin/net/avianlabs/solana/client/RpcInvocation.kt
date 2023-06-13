package net.avianlabs.solana.client

import kotlinx.serialization.json.JsonArray

public data class RpcInvocation(
  public val method: String,
  public val params: JsonArray?,
)
