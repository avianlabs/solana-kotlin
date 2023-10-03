package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.domain.core.PublicKey

/**
 * Requests an airdrop of lamports to a Pubkey
 *
 * @param publicKey Pubkey of account to receive lamports
 * @param lamports lamports to airdrop, as a "u64"
 * @param commitment Optional [Commitment] level
 */
public suspend fun SolanaClient.requestAirdrop(
  publicKey: PublicKey,
  lamports: Long,
  commitment: Commitment? = null
): String {
  val result = invoke<String>(
    method = "requestAirdrop",
    params = JsonArray(buildList {
      add(json.encodeToJsonElement<PublicKey>(publicKey))
      add(json.encodeToJsonElement<Long>(lamports))
      commitment?.let { json.encodeToJsonElement(RequestAirdropParams(it.value)) }
    })
  )
  return result!!
}

@Serializable
internal data class RequestAirdropParams(
  val commitment: String? = null,
)
