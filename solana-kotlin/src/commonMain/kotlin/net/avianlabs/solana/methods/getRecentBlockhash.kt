package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcResponse
import net.avianlabs.solana.domain.core.Commitment

public suspend fun SolanaClient.getRecentBlockhash(
  commitment: Commitment? = null,
): RecentBlockHash {
  val rpc = invoke<RpcResponse.RPC<RecentBlockHash>>("getRecentBlockhash", params(commitment))
  return rpc!!.value!!
}

private fun SolanaClient.params(
  commitment: Commitment?,
) = JsonArray(buildList {
  commitment?.let { add(json.encodeToJsonElement(it.value)) }
})

@Serializable
public data class RecentBlockHash(
  val blockhash: String,
  val feeCalculator: FeeCalculator,
) {

  @Serializable
  public data class FeeCalculator(
    val lamportsPerSignature: Long = 0,
  )
}
