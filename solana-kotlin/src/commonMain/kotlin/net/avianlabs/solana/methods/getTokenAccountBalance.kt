package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcResponse.RPC
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

/**
 * Returns the token balance of an SPL Token account.
 *
 * @param tokenAccount Pubkey of Token account to query
 * @param commitment Optional [Commitment] level
 *
 */
public suspend fun SolanaClient.getTokenAccountBalance(
  tokenAccount: PublicKey,
  commitment: Commitment? = null,
): TokenAmountInfo {
  val result = invoke<RPC<TokenAmountInfo>>(
    method = "getTokenAccountBalance",
    params = JsonArray(buildList {
      add(json.encodeToJsonElement<PublicKey>(tokenAccount))
      commitment?.let { add(json.encodeToJsonElement(TokenAccountBalanceParams(it.value))) }
    })
  )
  return result!!.value!!
}

@Serializable
internal data class TokenAccountBalanceParams(
  val commitment: String? = null
)

@Serializable
public data class TokenAmountInfo(
  val amount: String?,
  val decimals: Int,
  val uiAmount: Double?,
  val uiAmountString: String,
)
