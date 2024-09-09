package net.avianlabs.solana.methods

import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.Response
import net.avianlabs.solana.client.Response.RPC
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

/**
 * @return The balance of the account of provided [PublicKey]
 *
 * @param account Pubkey of account to query
 * @param commitment Optional [Commitment] level
 */
public suspend fun SolanaClient.getBalance(
  account: PublicKey,
  commitment: Commitment? = null,
): Response<RPC<Long>> = invoke(
  method = "getBalance",
  params = buildJsonArray {
    add(account.toBase58())
    commitment?.let {
      addJsonObject {
        put("commitment", it.value)
      }
    }
  }
)
