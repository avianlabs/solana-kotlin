package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

@ConsistentCopyVisibility
public data class Message private constructor(
  public val feePayer: PublicKey?,
  public val recentBlockHash: String?,
  public val accountKeys: List<AccountMeta>,
  public val instructions: List<TransactionInstruction>,
) {

  public fun newBuilder(): Builder = Builder(
    feePayer = feePayer,
    recentBlockHash = recentBlockHash,
    accountKeys = accountKeys.toMutableList(),
    instructions = instructions.toMutableList(),
  )

  public class Builder internal constructor(
    private var feePayer: PublicKey?,
    private var recentBlockHash: String?,
    private var accountKeys: MutableList<AccountMeta>,
    private var instructions: MutableList<TransactionInstruction>,
  ) {
    public constructor() : this(null, null, mutableListOf(), mutableListOf())

    public fun setFeePayer(feePayer: PublicKey): Builder {
      this.feePayer = feePayer
      accountKeys.add(AccountMeta(feePayer, isSigner = true, isWritable = true))
      return this
    }

    public fun setRecentBlockHash(recentBlockHash: String): Builder {
      this.recentBlockHash = recentBlockHash
      return this
    }

    public fun addInstruction(instruction: TransactionInstruction): Builder {
      accountKeys.addAll(
        instruction.keys +
          AccountMeta(instruction.programId, isSigner = false, isWritable = false)
      )
      instructions += instruction
      return this
    }

    public fun build(): Message =
      Message(feePayer, recentBlockHash, accountKeys.normalize(feePayer), instructions)
  }
}
