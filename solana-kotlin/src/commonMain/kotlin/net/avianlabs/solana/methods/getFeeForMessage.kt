package net.avianlabs.solana.methods

import io.ktor.util.*
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
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
    params = buildJsonArray {
      add(message.encodeBase64())
      commitment?.let {
        addJsonObject {
          put("commitment", it.value)
        }
      }
    }
  )
  return result!!.value!!
}
