package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.domain.core.PublicKey

public suspend fun SolanaClient.getSignaturesForAddress(
  account: PublicKey,
  commitment: String?,
): List<SignatureInformation> {
  val rpcParams = SignaturesForAddress(
    commitment = commitment,
  )
  val result =
    invoke<List<SignatureInformation>>("getSignaturesForAddress", params(account, rpcParams))
  return result!!
}

private fun SolanaClient.params(
  account: PublicKey,
  rpcParams: SignaturesForAddress,
) = JsonArray(listOf(json.encodeToJsonElement(account), json.encodeToJsonElement(rpcParams)))

@Serializable
internal data class SignaturesForAddress(
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
