package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.domain.core.PublicKey

/**
 * Returns Signatures for confirmed transactions that include
 * the given address in their accountKeys list.
 * Returns signatures backwards in time from the provided signature or
 * most recent confirmed block
 *
 * @param account Account address
 * @param commitment Optional [Commitment] level
 */
public suspend fun SolanaClient.getSignaturesForAddress(
  account: PublicKey,
  commitment: Commitment? = null,
): List<SignatureInformation> {
  val result = invoke<List<SignatureInformation>>(
    method = "getSignaturesForAddress",
    params = JsonArray(buildList {
      add(json.encodeToJsonElement<PublicKey>(account))
      commitment?.let {
        add(json.encodeToJsonElement(SignaturesForAddressParams(commitment = it.value)))
      }
    })
  )
  return result!!
}

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
