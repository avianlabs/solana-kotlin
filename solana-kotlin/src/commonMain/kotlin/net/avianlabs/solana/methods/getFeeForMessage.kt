package net.avianlabs.solana.methods

import io.ktor.util.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcResponse.RPC

public suspend fun SolanaClient.getFeeForMessage(message: ByteArray): Long {
  val result = invoke<RPC<Long>>("getFeeForMessage", params(message))
  return result!!.value!!
}

private fun SolanaClient.params(
  message: ByteArray,
) = JsonArray(listOf(json.encodeToJsonElement(message.encodeBase64())))
