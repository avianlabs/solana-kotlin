package net.avianlabs.solana.methods

import io.ktor.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.Response
import net.avianlabs.solana.client.Response.RPC
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.domain.core.Transaction

/**
 * Simulate a transaction and return the result
 *
 * @param transaction Transaction to simulate
 * @param commitment Commitment level to simulate the transaction at
 * @param sigVerify if true the transaction signatures will be verified (conflicts with
 * replaceRecentBlockhash)
 * @param replaceRecentBlockhash if true the transaction recent blockhash will be replaced with the
 * most recent blockhash. (conflicts with sigVerify)
 * @param minContextSlot the minimum slot that the request can be evaluated at
 * @param innerInstructions If true the response will include inner instructions. These inner
 * instructions will be jsonParsed where possible, otherwise json.
 * @param accounts Accounts configuration object
 *
 * @return Simulated transaction result
 */
public suspend fun SolanaClient.simulateTransaction(
  transaction: Transaction,
  commitment: Commitment? = null,
  sigVerify: Boolean? = null,
  replaceRecentBlockhash: Boolean? = null,
  minContextSlot: ULong? = null,
  innerInstructions: Boolean? = null,
  accounts: List<String>? = null,
): Response<RPC<SimulateTransactionResponse>> = invoke(
  method = "simulateTransaction",
  params = buildJsonArray {
    add(transaction.sign(emptyList()).serialize().encodeBase64())
    addJsonObject {
      put("encoding", "base64")
      commitment?.let { put("commitment", it.value) }
      sigVerify?.let { put("sigVerify", it) }
      replaceRecentBlockhash?.let { put("replaceRecentBlockhash", it) }
      minContextSlot?.let { put("minContextSlot", it.toString()) }
      innerInstructions?.let { put("innerInstructions", it) }
      accounts?.let {
        putJsonObject("accounts") {
          putJsonArray("addresses") {
            it.forEach { add(it) }
          }
          put("encoding", "base58")
        }
      }
    }
  }
)

@Serializable
public data class SimulateTransactionResponse(
  /**
   * Error if transaction failed, null if transaction succeeded.
   *
   * can be null, string or object
   */
  val err: JsonElement?,
  /**
   * Array of log messages the transaction instructions output during execution, null if simulation
   * failed before the transaction was able to execute (for example due to an invalid blockhash or
   * signature verification failure)
   */
  val logs: List<String>?,
  /**
   * Accounts requested if any
   */
  val accounts: List<AccountInfo>?,
  /**
   * The number of compute budget units consumed during the processing of this transaction
   */
  val unitsConsumed: ULong,
  /**
   * the most-recent return data generated by an instruction in the transaction
   */
  val returnData: ReturnData?,
  /**
   * Defined only if innerInstructions was set to true
   */
  val innerInstructions: JsonElement?,
) {
  @Serializable
  public data class AccountInfo(
    /**
     * Number of lamports assigned to this account
     */
    val lamports: ULong,
    /**
     * Base-58 encoded Pubkey of the program this account has been assigned to
     */
    val owner: String,
    /**
     * Data associated with the account, either as encoded binary data or JSON format
     */
    val data: JsonElement,
    /**
     * Boolean indicating if the account contains a program (and is strictly read-only)
     */
    val executable: Boolean,
    /**
     * The epoch at which this account will next owe rent
     */
    val rentEpoch: ULong,
  )

  @Serializable
  public data class ReturnData(
    /**
     * The program that generated the return data
     */
    val programId: String,
    /**
     * The return data itself, as base-64 encoded binary data
     */
    val data: String,
  )
}
