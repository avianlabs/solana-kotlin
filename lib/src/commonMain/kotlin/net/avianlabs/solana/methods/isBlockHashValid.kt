package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcResponse.RPC

public suspend fun SolanaClient.isBlockHashValid(
  blockHash: String,
  commitment: String? = null,
  minContextSlot: Long? = null,
): Boolean {
  val result =
    invoke<RPC<Boolean>>("isBlockHashValid", params(blockHash, commitment, minContextSlot))
  return result!!.value!!
}

private fun SolanaClient.params(
  blockHash: String,
  commitment: String? = null,
  minContextSlot: Long? = null,
) = JsonArray(
  listOf(
    json.encodeToJsonElement(blockHash),
    json.encodeToJsonElement(IsBlockHashValidParams(commitment, minContextSlot))
  )
)

@Serializable
internal data class IsBlockHashValidParams(
  val commitment: String? = null,
  val minContextSlot: Long? = null,
)
