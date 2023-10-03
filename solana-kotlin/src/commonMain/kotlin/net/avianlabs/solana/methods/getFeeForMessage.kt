package net.avianlabs.solana.methods

import io.ktor.util.encodeBase64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcResponse.RPC
import net.avianlabs.solana.domain.core.Commitment

/**
 * Get the fee the network will charge for a particular Message
 *
 * @param message Base-64 encoded Message
 * @param commitment Optional [Commitment] level
 *
 */
public suspend fun SolanaClient.getFeeForMessage(
  message: ByteArray,
  commitment: Commitment? = null
): Long {
  val result = invoke<RPC<Long>>(
    method = "getFeeForMessage",
    params = JsonArray(buildList {
      add(json.encodeToJsonElement<String>(message.encodeBase64()))
      commitment?.let { add(json.encodeToJsonElement(FeeForMessageParams(it.value))) }
    })
  )
  return result!!.value!!
}

@Serializable
internal data class FeeForMessageParams(
  val commitment: String? = null,
)
