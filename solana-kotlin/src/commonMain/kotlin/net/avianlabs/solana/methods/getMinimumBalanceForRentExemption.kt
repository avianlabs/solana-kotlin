package net.avianlabs.solana.methods

import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.domain.core.Commitment

/**
 * Returns minimum balance required to make account rent exempt.
 *
 * @param dataLength the Account's data length
 * @param commitment Optional [Commitment] level
 */
public suspend fun SolanaClient.getMinimumBalanceForRentExemption(
  dataLength: Long,
  commitment: Commitment? = null,
): Long {
  val result = invoke<Long>(
    method = "getMinimumBalanceForRentExemption",
    params = buildJsonArray {
      add(dataLength)
      commitment?.let {
        addJsonObject {
          put("commitment", it.value)
        }
      }
    }
  )
  return result!!
}
