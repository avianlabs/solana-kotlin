package net.avianlabs.solana.domain.program

import kotlin.UByte
import net.avianlabs.solana.domain.core.AccountMeta
import net.avianlabs.solana.domain.core.TransactionInstruction
import net.avianlabs.solana.domain.program.Program.Companion.createTransactionInstruction
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import okio.Buffer

public object AssociatedTokenProgram : Program {
  public override val programId: PublicKey =
      PublicKey.fromBase58("ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL")

  public fun createAssociatedToken(
    ata: PublicKey,
    owner: PublicKey,
    mint: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(owner, isSigner = false, isWritable = false),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(SystemProgram.programId, isSigner = false, isWritable = false),
      AccountMeta(TokenProgram.programId, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.CreateAssociatedToken.index.toInt())
    .readByteArray(),
  )

  public fun createAssociatedTokenIdempotent(
    ata: PublicKey,
    owner: PublicKey,
    mint: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(owner, isSigner = false, isWritable = false),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(SystemProgram.programId, isSigner = false, isWritable = false),
      AccountMeta(TokenProgram.programId, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.CreateAssociatedTokenIdempotent.index.toInt())
    .readByteArray(),
  )

  public fun recoverNestedAssociatedToken(
    nestedAssociatedAccountAddress: PublicKey,
    nestedTokenMintAddress: PublicKey,
    destinationAssociatedAccountAddress: PublicKey,
    ownerAssociatedAccountAddress: PublicKey,
    ownerTokenMintAddress: PublicKey,
    walletAddress: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(nestedTokenMintAddress, isSigner = false, isWritable = false),
      AccountMeta(ownerTokenMintAddress, isSigner = false, isWritable = false),
      AccountMeta(walletAddress, isSigner = true, isWritable = true),
      AccountMeta(TokenProgram.programId, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.RecoverNestedAssociatedToken.index.toInt())
    .readByteArray(),
  )

  public enum class Instruction(
    public val index: UByte,
  ) {
    CreateAssociatedToken(0u),
    CreateAssociatedTokenIdempotent(1u),
    RecoverNestedAssociatedToken(2u),
    ;
  }
}
