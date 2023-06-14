package net.avianlabs.solana.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlin.collections.set

public class RpcKtorClient(
  internal val url: Url,
  httpClient: HttpClient = HttpClient(),
) {

  public constructor(
    url: String,
    httpClient: HttpClient = HttpClient(),
  ) : this(
    url = Url(url),
    httpClient = httpClient,
  )

  internal val requestIdGenerator: RequestIdGenerator = RequestIdGenerator()

  internal val json: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    allowSpecialFloatingPointValues = true
  }

  internal val ktorClient: HttpClient = httpClient.config {
    install(ContentNegotiation) { json(json = json) }
    expectSuccess = true
    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.ALL
    }
  }

  internal suspend inline fun <reified T, reified R> invoke(invocation: RpcInvocation<T>): RpcResponse<R> =
    execute(makeRequest(invocation))

  internal inline fun <reified T> makeRequest(invocation: RpcInvocation<T>): RpcRequest<T> =
    RpcRequest(requestIdGenerator.next(), invocation)

  internal suspend inline fun <reified T, reified R> execute(request: RpcRequest<T>): RpcResponse<R> {
    val response = ktorClient.post(url) {
      contentType(ContentType.Application.Json)
      setBody(request.buildBody())
    }
    response.body<JsonObject>()["error"]?.let {
      throw ExecuteException(Json.decodeFromJsonElement<RpcError>(it))
    }
    return response.body()
  }

  internal inline fun <reified T> RpcRequest<T>.buildBody(): JsonObject {
    val body: MutableMap<String, JsonElement> = mutableMapOf(
      "jsonrpc" to JsonPrimitive(HttpRequestExecutorConfig.version),
      "method" to JsonPrimitive(invocation.method)
    )
    body["params"] = invocation.params
      ?.let { json.encodeToJsonElement(it).jsonArray }
      ?: JsonArray(emptyList())
    id?.let { body["id"] = JsonPrimitive(it) }
    return JsonObject(body)
  }
}
