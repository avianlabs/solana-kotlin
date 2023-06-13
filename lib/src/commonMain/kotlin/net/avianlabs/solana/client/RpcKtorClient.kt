package net.avianlabs.solana.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

public class RpcKtorClient(
  private val url: String,
) {

  private val requestIdGenerator = RequestIdGenerator()

  private val ktorClient: HttpClient = HttpClient {
    install(ContentNegotiation) { json() }
    expectSuccess = true
    developmentMode = true
    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.ALL
    }
  }

  public suspend fun invoke(invocation: RpcInvocation): RpcResponse =
    execute(makeRequest(invocation))

  private fun makeRequest(invocation: RpcInvocation) =
    RpcRequest(requestIdGenerator.next(), invocation)

  private suspend fun execute(request: RpcRequest): RpcResponse {

    val response = ktorClient.post(url) {
      contentType(ContentType.Application.Json)
      setBody(request.buildBody())
    }
    response.body<JsonObject>()["error"]?.let {
      throw ExecuteException(Json.decodeFromJsonElement<RpcError>(it))
    }
    return response.body()
  }
}
