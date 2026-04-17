package net.avianlabs.solana

import io.ktor.client.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import net.avianlabs.solana.client.Response
import net.avianlabs.solana.client.RpcInvocation
import net.avianlabs.solana.client.RpcKtorClient

public class SolanaClient(
  private val client: RpcKtorClient,
  private val headerProviders: Map<String, suspend () -> String?> = mapOf(),
) {

  public constructor(
    url: String,
    authorizationProvider: (suspend () -> String?)? = null,
  ) : this(
    client = RpcKtorClient(url = url, httpClient = HttpClient()),
    headerProviders = buildMap {
      if (authorizationProvider != null) {
        put(HttpHeaders.Authorization, authorizationProvider)
      }
    },
  )

  internal val json: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    allowSpecialFloatingPointValues = true
    explicitNulls = false
  }

  internal suspend inline fun <reified T> invoke(
    method: String,
    params: JsonArray? = null,
  ): Response<T> {
    val invocation = RpcInvocation(method, params, headerProviders)
    return client.invoke<JsonArray, T>(invocation)
  }
}
