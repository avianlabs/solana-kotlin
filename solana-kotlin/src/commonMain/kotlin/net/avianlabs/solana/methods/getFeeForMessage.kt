package net.avianlabs.solana.methods

import io.ktor.util.encodeBase64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcResponse.RPC
import net.avianlabs.solana.domain.core.Commitment

public suspend fun SolanaClient.getFeeForMessage(
  message: ByteArray,
  commitment: Commitment? = null
): Long {
  val result = invoke<RPC<Long>>("getFeeForMessage", params(message, commitment))
  return result!!.value!!
}

private fun SolanaClient.params(
  message: ByteArray,
  commitment: Commitment?,
) = JsonArray(buildList {
  add(json.encodeToJsonElement(message.encodeBase64()))
  commitment?.let { add(json.encodeToJsonElement(FeeForMessageParams(it.value))) }
})

@Serializable
internal data class FeeForMessageParams(
  val commitment: String? = null,
)
