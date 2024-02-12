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
    programId: PublicKey,
    mint: PublicKey,
    associatedAccount: PublicKey,
    owner: PublicKey,
    payer: PublicKey,
  ): TransactionInstruction = buildCreateAssociatedTokenAccountInstruction(
    payer = payer,
    associatedAccount = associatedAccount,
    owner = owner,
    mint = mint,
    programId = programId,
    associatedProgramId = associatedProgramId,
    bytes = byteArrayOf(),
  )

  public fun createAssociatedTokenAccountInstructionIdempotent(
    associatedProgramId: PublicKey = ASSOCIATED_TOKEN_PROGRAM_PROGRAM_ID,
    programId: PublicKey,
    mint: PublicKey,
    associatedAccount: PublicKey,
    owner: PublicKey,
    payer: PublicKey,
  ): TransactionInstruction = buildCreateAssociatedTokenAccountInstruction(
    payer = payer,
    associatedAccount = associatedAccount,
    owner = owner,
    mint = mint,
    programId = programId,
    associatedProgramId = associatedProgramId,
    bytes = byteArrayOf(1),
  )

  private fun buildCreateAssociatedTokenAccountInstruction(
    payer: PublicKey,
    associatedAccount: PublicKey,
    owner: PublicKey,
    mint: PublicKey,
    programId: PublicKey,
    associatedProgramId: PublicKey,
    bytes: ByteArray,
  ): TransactionInstruction {
    val keys = listOf(
      AccountMeta(payer, isSigner = true, isWritable = true),
      AccountMeta(associatedAccount, isSigner = false, isWritable = true),
      AccountMeta(owner, isSigner = false, isWritable = false),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(SystemProgram.programId, isSigner = false, isWritable = false),
      AccountMeta(programId, isSigner = false, isWritable = false),
      AccountMeta(SystemProgram.SYSVAR_RENT_ACCOUNT, isSigner = false, isWritable = false)
    )

    return TransactionInstruction(
      keys = keys,
      programId = associatedProgramId,
      data = bytes,
    )
  }
}

public fun PublicKey.associatedTokenAddress(
  tokenMintAddress: PublicKey,
): ProgramDerivedAddress = Program.findProgramAddress(
  listOf(
    bytes.copyOf(),
    TokenProgram.programId.toByteArray(),
    tokenMintAddress.toByteArray()
  ),
  AssociatedTokenProgram.programId,
)
