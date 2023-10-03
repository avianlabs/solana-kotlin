package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
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
    params = JsonArray(buildList {
      add(json.encodeToJsonElement(dataLength))
      commitment?.let { add(json.encodeToJsonElement(GetMinimumBalanceForRentExemptionParams(it.value))) }
    })
  )
  return result!!
}

@Serializable
internal data class GetMinimumBalanceForRentExemptionParams(
  val commitment: String? = null,
)
