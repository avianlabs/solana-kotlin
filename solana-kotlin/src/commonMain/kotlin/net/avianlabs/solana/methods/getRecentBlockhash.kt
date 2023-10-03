package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcResponse
import net.avianlabs.solana.domain.core.Commitment

/**
 * Returns the latest blockhash
 *
 * @param commitment Optional [Commitment] level
 */
public suspend fun SolanaClient.getRecentBlockhash(
  commitment: Commitment? = null,
): RecentBlockHash {
  val result = invoke<RpcResponse.RPC<RecentBlockHash>>(
    method = "getRecentBlockhash",
    params = JsonArray(buildList {
      commitment?.let { add(json.encodeToJsonElement(it.value)) }
    })
  )
  return result!!.value!!
}

@Serializable
public data class RecentBlockHash(
  val blockhash: String,
  val feeCalculator: FeeCalculator,
) {

  @Serializable
  public data class FeeCalculator(
    val lamportsPerSignature: Long = 0,
  )
}
