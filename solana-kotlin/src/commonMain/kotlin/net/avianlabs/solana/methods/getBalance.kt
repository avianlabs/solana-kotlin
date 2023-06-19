package net.avianlabs.solana.methods

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcResponse.RPC
import net.avianlabs.solana.domain.core.PublicKey

public suspend fun SolanaClient.getBalance(
  account: PublicKey,
): Long {
  val result = invoke<RPC<Long>>("getBalance", params(account))
  return result!!.value!!
}

private fun SolanaClient.params(
  account: PublicKey,
) = JsonArray(listOf(json.encodeToJsonElement(account)))
