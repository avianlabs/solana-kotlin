package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient

public suspend fun SolanaClient.getMinimumBalanceForRentExemption(
  dataLength: Long,
  commitment: String = "confirmed",
): Long {
  val result = invoke<Long>("getMinimumBalanceForRentExemption", params(dataLength, commitment))
  return result!!
}

private fun SolanaClient.params(
  dataLength: Long,
  commitment: String,
) = JsonArray(
  listOf(
    json.encodeToJsonElement(dataLength),
    json.encodeToJsonElement(GetMinimumBalanceForRentExemptionParams(commitment))
  )
)

@Serializable
internal data class GetMinimumBalanceForRentExemptionParams(
  val commitment: String,
)
