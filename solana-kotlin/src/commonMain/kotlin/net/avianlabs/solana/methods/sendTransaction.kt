package net.avianlabs.solana.methods

import io.ktor.util.*
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.Response
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.domain.core.Transaction

/**
 * Send a signed transaction to the cluster
 *
 * @param transaction The signed transaction to send
 * @param skipPreflight If true, skip the preflight check
 * @param preflightCommitment The commitment level to use for the preflight check
 * @param maxRetries The maximum number of retries to send the transaction
 * @param minContextSlot The minimum slot to send the transaction
 * @return The transaction signature
 */
public suspend fun SolanaClient.sendTransaction(
  transaction: Transaction,
  skipPreflight: Boolean = false,
  preflightCommitment: Commitment = Commitment.Finalized,
  maxRetries: Int? = null,
  minContextSlot: Long? = null,
): Response<String> = invoke(
  method = "sendTransaction",
  params = buildJsonArray {
    add(transaction.serialize().encodeBase64())
    add(buildJsonObject {
      put("encoding", "base64")
      put("skipPreflight", skipPreflight)
      put("preflightCommitment", preflightCommitment.value)
      maxRetries?.let { put("maxRetries", it) }
      minContextSlot?.let { put("minContextSlot", it) }
    })
  }
)
