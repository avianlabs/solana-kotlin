package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcResponse

public suspend fun SolanaClient.getRecentBlockhash(
  commitment: String? = null,
): RecentBlockHash {
  val rpc = invoke<RpcResponse.RPC<RecentBlockHash>>("getRecentBlockhash", params(commitment))
  return rpc!!.value!!
}

private fun SolanaClient.params(
  commitment: String? = null,
) = JsonArray(listOf(json.encodeToJsonElement(commitment)))

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
