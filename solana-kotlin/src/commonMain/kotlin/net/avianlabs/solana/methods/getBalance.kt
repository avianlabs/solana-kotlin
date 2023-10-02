package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcResponse.RPC
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.domain.core.PublicKey

public suspend fun SolanaClient.getBalance(
  account: PublicKey,
  commitment: Commitment? = null,
): Long {
  val result = invoke<RPC<Long>>("getBalance", params(account, commitment))
  return result!!.value!!
}

private fun SolanaClient.params(
  account: PublicKey,
  commitment: Commitment?
) = JsonArray(buildList {
  add(json.encodeToJsonElement(account))
  commitment?.let { add(json.encodeToJsonElement(BalanceParams(it.value))) }
})

@Serializable
internal data class BalanceParams(
  val commitment: String? = null,
)
