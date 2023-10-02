package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.domain.core.PublicKey

public suspend fun SolanaClient.getSignaturesForAddress(
  account: PublicKey,
  commitment: Commitment? = null,
): List<SignatureInformation> {
  val result = invoke<List<SignatureInformation>>(
    "getSignaturesForAddress",
    params(account, commitment)
  )
  return result!!
}

private fun SolanaClient.params(
  account: PublicKey,
  commitment: Commitment?
) = JsonArray(buildList {
  add(json.encodeToJsonElement(account))
  commitment?.let { add(json.encodeToJsonElement(SignaturesForAddressParams(commitment = it.value))) }
})

@Serializable
internal data class SignaturesForAddressParams(
  val limit: Long? = null,
  val before: String? = null,
  val until: String? = null,
  val commitment: String? = null,
  val minContextSlot: Long? = null,
)

@Serializable
public data class SignatureInformation(
  var err: JsonElement?, // TODO
  val memo: JsonElement?, // TODO
  val signature: String?,
  val slot: Long?,
  val blockTime: Long?,
  val confirmationStatus: String?,
)
