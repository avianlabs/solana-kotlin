package net.avianlabs.solana.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
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
      request.invocation.headerProviders.forEach { (header, valueProvider) ->
        header(header, valueProvider())
      }
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
