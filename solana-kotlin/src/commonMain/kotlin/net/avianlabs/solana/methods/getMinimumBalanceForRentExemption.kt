package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.domain.core.Commitment

public suspend fun SolanaClient.getMinimumBalanceForRentExemption(
  dataLength: Long,
  commitment: Commitment? = null,
): Long {
  val result = invoke<Long>("getMinimumBalanceForRentExemption", params(dataLength, commitment))
  return result!!
}

private fun SolanaClient.params(
  dataLength: Long,
  commitment: Commitment?
) = JsonArray(buildList {
  add(json.encodeToJsonElement(dataLength))
  commitment?.let { add(json.encodeToJsonElement(GetMinimumBalanceForRentExemptionParams(it.value))) }
})

@Serializable
internal data class GetMinimumBalanceForRentExemptionParams(
  val commitment: String? = null,
)
