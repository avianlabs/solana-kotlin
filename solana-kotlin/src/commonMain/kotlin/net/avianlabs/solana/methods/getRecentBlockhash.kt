package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.Response
import net.avianlabs.solana.client.Response.RPC
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.domain.core.FeeCalculator

/**
 * Returns the latest blockhash
 *
 * @param commitment Optional [Commitment] level
 *
 * @deprecated Use [getLatestBlockhash] instead
 */
@Deprecated(
  "No longer a part of solana-core after 2.0. Use getLatestBlockhash instead",
  ReplaceWith("getLatestBlockhash"),
  DeprecationLevel.ERROR,
)
public suspend fun SolanaClient.getRecentBlockhash(
  commitment: Commitment? = null,
): Response<RPC<RecentBlockHash>> = invoke(
  method = "getRecentBlockhash",
  params = buildJsonArray {
    commitment?.let {
      addJsonObject {
        put("commitment", it.value)
      }
    }
  }
)

@Serializable
public data class RecentBlockHash(
  val blockhash: String,
  val feeCalculator: FeeCalculator,
)
