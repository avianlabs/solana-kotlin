package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.Response
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

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
): Response<List<SignatureInformation>> = invoke(
  method = "getSignaturesForAddress",
  params = buildJsonArray {
    add(account.toBase58())
    commitment?.let {
      addJsonObject {
        put("commitment", it.value)
      }
    }
  }
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
