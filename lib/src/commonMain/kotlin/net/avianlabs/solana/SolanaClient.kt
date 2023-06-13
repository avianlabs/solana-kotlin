package net.avianlabs.solana

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import net.avianlabs.solana.client.RpcInvocation
import net.avianlabs.solana.client.RpcKtorClient

public class SolanaService(
  private val client: RpcKtorClient,
) {

  internal val json: Json = Json

  internal suspend fun invoke(
    method: String,
    params: JsonArray? = null,
  ): JsonElement? {
    val invocation = makeInvocation(method, params)
    return client.invoke(invocation).result
  }

  private fun makeInvocation(
    method: String,
    params: JsonArray? = null,
  ): RpcInvocation = RpcInvocation(method, params)
}
