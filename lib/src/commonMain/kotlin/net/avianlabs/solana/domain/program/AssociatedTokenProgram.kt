package net.avianlabs.solana.domain.program

import net.avianlabs.solana.domain.core.AccountMeta
import net.avianlabs.solana.domain.core.PublicKey
import net.avianlabs.solana.domain.core.TransactionInstruction

private val ASSOCIATED_TOKEN_PROGRAM_PROGRAM_ID =
  PublicKey.fromBase58("ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL")

public object AssociatedTokenProgram : Program(
  programId = ASSOCIATED_TOKEN_PROGRAM_PROGRAM_ID
) {

  public fun createAssociatedTokenAccountInstruction(
    associatedProgramId: PublicKey = ASSOCIATED_TOKEN_PROGRAM_PROGRAM_ID,
    programId: PublicKey = TokenProgram.programId,
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
      AccountMeta(SystemProgram.programId, isSigner = false, isWritable = false),
      AccountMeta(programId, isSigner = false, isWritable = false),
      AccountMeta(TokenProgram.sysvarRentAccount, isSigner = false, isWritable = false)
    )

    return TransactionInstruction(
      keys = keys,
      programId = associatedProgramId,
      data = byteArrayOf(),
    )
  }
}

public fun PublicKey.associatedTokenAddress(
  tokenMintAddress: PublicKey,
): ProgramDerivedAddress = Program.findProgramAddress(
  listOf(
    bytes,
    TokenProgram.programId.toByteArray(),
    tokenMintAddress.toByteArray()
  ),
  AssociatedTokenProgram.programId,
)
