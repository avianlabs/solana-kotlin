package net.avianlabs.solana.domain.program

import net.avianlabs.solana.domain.core.AccountMeta
import net.avianlabs.solana.domain.core.PublicKey
import net.avianlabs.solana.domain.core.TransactionInstruction

public object AssociatedTokenProgram : Program() {

  public val SPL_ASSOCIATED_TOKEN_ACCOUNT_PROGRAM_ID: PublicKey =
    PublicKey.fromBase58("ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL")

  public fun createAssociatedTokenAccountInstruction(
    associatedProgramId: PublicKey = SPL_ASSOCIATED_TOKEN_ACCOUNT_PROGRAM_ID,
    programId: PublicKey = TokenProgram.PROGRAM_ID,
    mint: PublicKey,
    associatedAccount: PublicKey,
    owner: PublicKey,
    payer: PublicKey,
  ): TransactionInstruction {

    val keys = listOf(
      AccountMeta(payer, isSigner = true, isWritable = true),
      AccountMeta(associatedAccount, isSigner = false, isWritable = true),
      AccountMeta(owner, isSigner = false, isWritable = false),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(SystemProgram.PROGRAM_ID, isSigner = false, isWritable = false),
      AccountMeta(programId, isSigner = false, isWritable = false),
      AccountMeta(TokenProgram.SYSVAR_RENT_PUBKEY, isSigner = false, isWritable = false)
    )

    return TransactionInstruction(
      keys = keys,
      programId = associatedProgramId,
      data = byteArrayOf(),
    )
  }
}
