package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcResponse.RPC
import net.avianlabs.solana.domain.core.PublicKey

public suspend fun SolanaClient.getTokenAccountBalance(
  tokenAccount: PublicKey,
): TokenAmountInfo {
  val result = invoke<RPC<TokenAmountInfo>>("getTokenAccountBalance", params(tokenAccount))
  return result!!.value!!
}

private fun SolanaClient.params(
  account: PublicKey,
) = JsonArray(listOf(json.encodeToJsonElement(account)))

@Serializable
public data class TokenAmountInfo(
  val amount: String?,
  val decimals: Int,
  val uiAmount: Double?,
  val uiAmountString: String,
)
