package net.avianlabs.solana

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import net.avianlabs.solana.client.RpcInvocation
import net.avianlabs.solana.client.RpcKtorClient

public class SolanaClient(
  private val client: RpcKtorClient,
) {

  @OptIn(ExperimentalSerializationApi::class)
  internal val json: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    allowSpecialFloatingPointValues = true
    explicitNulls = false
  }

  internal suspend inline fun <reified T> invoke(
    method: String,
    params: JsonArray? = null,
  ): T? {
    val invocation = RpcInvocation(method, params)
    return client.invoke<JsonArray, T>(invocation).result
  }
}
