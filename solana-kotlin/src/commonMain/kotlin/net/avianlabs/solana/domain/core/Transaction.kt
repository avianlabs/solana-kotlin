package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

@Deprecated(
  message = "Use VersionedTransaction instead",
  replaceWith = ReplaceWith("VersionedTransaction"),
)
public class Transaction internal constructor(
  public val message: Message,
) {

  public fun sign(signer: Signer): SignedTransaction = sign(listOf(signer))

  public fun sign(signers: List<Signer>): SignedTransaction {
    val message = when (message.feePayer) {
      // fee payer is the first signer by default
      null -> message.newBuilder()
        .setFeePayer(signers.first().publicKey)
        .build()

      else -> message
    }

    return SignedTransaction.sign(VersionedMessage.Legacy(message), signers)
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
