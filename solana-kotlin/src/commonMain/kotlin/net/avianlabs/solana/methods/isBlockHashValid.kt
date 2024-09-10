package net.avianlabs.solana.methods

import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.Response
import net.avianlabs.solana.client.Response.RPC
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
): Response<RPC<Boolean>> = invoke(
  method = "isBlockhashValid",
  params = buildJsonArray {
    add(blockHash)
    if (listOfNotNull(commitment, minContextSlot).isNotEmpty()) {
      addJsonObject {
        commitment?.let { put("commitment", it.value) }
        minContextSlot?.let { put("minContextSlot", it) }
      }
    }
  }
)
