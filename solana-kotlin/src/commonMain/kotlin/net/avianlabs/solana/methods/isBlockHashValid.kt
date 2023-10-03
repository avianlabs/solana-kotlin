package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcResponse.RPC
import net.avianlabs.solana.domain.core.Commitment

/**
 * Returns whether a blockhash is still valid or not
 *
 * @param blockHash the blockhash of the block to evaluate, as base-58 encoded string
 * @param commitment Optional [Commitment] level
 * @param minContextSlot Optional minimum slot that the request can be evaluated at
 *
 */
public suspend fun SolanaClient.isBlockHashValid(
  blockHash: String,
  commitment: Commitment? = null,
  minContextSlot: Long? = null,
): Boolean {
  val result = invoke<RPC<Boolean>>(
    method = "isBlockhashValid",
    params = params(blockHash, commitment, minContextSlot)
  )
  return result!!.value!!
}

private fun SolanaClient.params(
  blockHash: String,
  commitment: Commitment?,
  minContextSlot: Long?,
) = JsonArray(buildList {
  add(json.encodeToJsonElement(blockHash))
  if (commitment != null || minContextSlot != null) {
    add(json.encodeToJsonElement(IsBlockHashValidParams(commitment?.value, minContextSlot)))
  }
})

@Serializable
internal data class IsBlockHashValidParams(
  val commitment: String? = null,
  val minContextSlot: Long? = null,
)
