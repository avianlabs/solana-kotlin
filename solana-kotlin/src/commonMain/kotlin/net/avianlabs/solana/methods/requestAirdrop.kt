package net.avianlabs.solana.methods

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.domain.core.PublicKey

public suspend fun SolanaClient.requestAirdrop(
  publicKey: PublicKey,
  lamports: Long,
): String {
  val result = invoke<String>("requestAirdrop", params(publicKey, lamports))
  return result!!
}

private fun SolanaClient.params(
  publicKey: PublicKey,
  lamports: Long,
) = JsonArray(listOf(json.encodeToJsonElement(publicKey), json.encodeToJsonElement(lamports)))
