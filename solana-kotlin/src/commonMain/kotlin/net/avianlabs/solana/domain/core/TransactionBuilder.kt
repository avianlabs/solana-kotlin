package net.avianlabs.solana.domain.core

public class TransactionBuilder {
  private val transaction: Transaction = Transaction()
  public fun addInstruction(transactionInstruction: TransactionInstruction): TransactionBuilder {
    transaction.addInstruction(transactionInstruction)
    return this
  }

  public fun setRecentBlockHash(recentBlockHash: String): TransactionBuilder {
    transaction.setRecentBlockHash(recentBlockHash)
    return this
  }

  public fun setSigners(signers: List<Signer>): TransactionBuilder {
    transaction.sign(signers)
    return this
  }

  public fun build(): Transaction {
    return transaction
  }
}
