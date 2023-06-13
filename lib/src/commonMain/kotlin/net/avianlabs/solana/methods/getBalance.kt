package net.avianlabs.solana.methods

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import net.avianlabs.solana.SolanaService
import net.avianlabs.solana.client.RPC
import net.avianlabs.solana.domain.PublicKey

public suspend fun SolanaService.getBalance(
  account: PublicKey,
): Long {
  val result = invoke("getBalance", params(account))
  val rpc = json.decodeFromJsonElement<RPC<Long>>(result!!)
  return rpc.value!!
}

private fun SolanaService.params(
  account: PublicKey,
) = JsonArray(listOf(json.encodeToJsonElement(account)))
