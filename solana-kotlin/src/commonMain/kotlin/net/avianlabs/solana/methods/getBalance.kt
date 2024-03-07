package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcResponse.RPC
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.tweetnacl.crypto.PublicKey

/**
 * @return The balance of the account of provided [PublicKey]
 *
 * @param account Pubkey of account to query
 * @param commitment Optional [Commitment] level
 */
public suspend fun SolanaClient.getBalance(
  account: PublicKey,
  commitment: Commitment? = null,
): Long {
  val result = invoke<RPC<Long>>(
    method = "getBalance",
    params = JsonArray(buildList {
      add(json.encodeToJsonElement<PublicKey>(account))
      commitment?.let { add(json.encodeToJsonElement(BalanceParams(it.value))) }
    })
  )
  return result!!.value!!
}

@Serializable
internal data class BalanceParams(
  val commitment: String? = null,
)
