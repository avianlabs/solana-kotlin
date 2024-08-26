package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcResponse
import net.avianlabs.solana.domain.core.Commitment

/**
 * Returns the latest blockhash
 *
 * @param commitment Optional [Commitment] level
 */
public suspend fun SolanaClient.getLatestBlockhash(
  commitment: Commitment? = null,
): LatestBlockHash {
  val result = invoke<RpcResponse.RPC<LatestBlockHash>>(
    method = "getLatestBlockhash",
    params = buildJsonArray {
      commitment?.let {
        addJsonObject {
          put("commitment", it.value)
        }
      }
    }
  )
  return result!!.value!!
}

@Serializable
public data class LatestBlockHash(
  val blockhash: String,
  val lastValidBlockHeight: Long,
)
