package net.avianlabs.solana.methods

import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

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
    params = buildJsonArray {
      add(publicKey.toBase58())
      add(lamports)
      commitment?.let {
        addJsonObject {
          put("commitment", it.value)
        }
      }
    }
  )
  return result!!
}
