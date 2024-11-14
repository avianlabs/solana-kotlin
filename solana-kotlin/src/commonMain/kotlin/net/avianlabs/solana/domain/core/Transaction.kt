package net.avianlabs.solana.domain.core

import io.github.oshai.kotlinlogging.KotlinLogging
import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import net.avianlabs.solana.tweetnacl.vendor.encodeToBase58String

private val logger = KotlinLogging.logger {}

public open class Transaction internal constructor(
  public val message: Message,
) {

  public fun sign(signer: Signer): SignedTransaction = sign(listOf(signer))

  public open fun sign(signers: List<Signer>): SignedTransaction {
    val message = when (message.feePayer) {
      // fee payer is the first signer by default
      null -> message.newBuilder()
        .setFeePayer(signers.first().publicKey)
        .build()

      else -> message
    }

    val serializedMessage = message
      .serialize()

    val signatures = signers.map { signer ->
      TweetNaCl.Signature.sign(serializedMessage, signer.secretKey).encodeToBase58String()
    }

    val signatureSet = signatures.toSet()
    if (signatureSet.size != signatures.size) {
      logger.warn { "Duplicate signatures detected" }
    }

    return SignedTransaction(
      originalMessage = message,
      signedMessage = serializedMessage,
      signatures = signatures,
    )
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as Transaction

    return message == other.message
  }

  override fun hashCode(): Int {
    return message.hashCode()
  }

  override fun toString(): String = "Transaction(message=$message)"

  public fun newBuilder(): Builder = Builder(message.newBuilder())

  public class Builder internal constructor(
    private var messageBuilder: Message.Builder,
  ) {
    public constructor() : this(Message.Builder())

    public fun addInstruction(instruction: TransactionInstruction): Builder {
      messageBuilder.addInstruction(instruction)
      return this
    }

    public fun setRecentBlockHash(recentBlockHash: String): Builder {
      messageBuilder.setRecentBlockHash(recentBlockHash)
      return this
    }

    public fun setFeePayer(feePayer: PublicKey): Builder {
      messageBuilder.setFeePayer(feePayer)
      return this
    }

    public fun build(): Transaction = Transaction(messageBuilder.build())
  }
}
