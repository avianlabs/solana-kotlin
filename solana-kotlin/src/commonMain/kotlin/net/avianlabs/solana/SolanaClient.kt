package net.avianlabs.solana

import io.ktor.client.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import net.avianlabs.solana.client.Response
import net.avianlabs.solana.client.RpcInvocation
import net.avianlabs.solana.client.RpcKtorClient
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

public class SolanaClient(
  private val client: RpcKtorClient,
  private val headerProviders: Map<String, suspend () -> String?> = mapOf(),
) {

  // iOS friendly constructor
  public constructor(
    url: String,
    authorizationHeaderProvider: (completion: (String?) -> Unit) -> Unit,
  ) : this(
    client = RpcKtorClient(
      url = url,
    ),
    headerProviders = mapOf(
      HttpHeaders.Authorization to {
        suspendCoroutine { continuation ->
          authorizationHeaderProvider { header ->
            continuation.resume(header)
          }
        }
      },
    ),
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
