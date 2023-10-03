package net.avianlabs.solana.methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.domain.core.Commitment

/**
 * Returns transaction details for a confirmed transaction
 *
 * @param signature Transaction signature, as base-58 encoded string
 * @param commitment Optional [Commitment] level
 *
 */
public suspend fun SolanaClient.getTransaction(
  signature: String,
  commitment: Commitment? = null,
): TransactionResponse? {
  return invoke<TransactionResponse?>(
    method = "getTransaction",
    params = params(signature, commitment)
  )
}

private fun SolanaClient.params(
  signature: String,
  commitment: Commitment?
) = JsonArray(buildList {
  add(json.encodeToJsonElement(signature))
  commitment?.let { add(json.encodeToJsonElement(GetTransactionParams(it.value))) }
})

@Serializable
internal data class GetTransactionParams(
  val commitment: String? = null,
)

@Serializable
public data class TransactionResponse(
  val meta: Meta?,
  val slot: Long?,
  val transaction: Transaction?,
  val blockTime: Long?,
) {

  @Serializable
  public data class Header(
    val numReadonlySignedAccounts: Long,
    val numReadonlyUnsignedAccounts: Long,
    val numRequiredSignatures: Long,
  )

  @Serializable
  public data class Instruction(
    val accounts: List<Long>?,
    val data: String?,
    val programIdIndex: Long,
  )

  @Serializable
  public data class Message(
    val accountKeys: List<String>,
    val header: Header,
    val instructions: List<Instruction>,
    val recentBlockhash: String,
  )

  @Serializable
  public data class TokenBalance(
    val accountIndex: Long,
    val mint: String,
    val uiTokenAmount: TokenAmountInfo,
  )

  @Serializable
  public data class Meta(
    val err: JsonElement?, // TODO
    val fee: Long,
    val innerInstructions: List<InnerInstructionMeta>,
    val preTokenBalances: List<TokenBalance>,
    val postTokenBalances: List<TokenBalance>,
    val postBalances: List<Long>,
    val preBalances: List<Long>,
    val logMessages: List<String>?,
  ) {

    @Serializable
    public data class InnerInstructionMeta(
      val index: Long,
      val instructions: List<Instruction>,
    )
  }

  @Serializable
  public data class Transaction(
    val message: Message,
    val signatures: List<String>,
  )
}
