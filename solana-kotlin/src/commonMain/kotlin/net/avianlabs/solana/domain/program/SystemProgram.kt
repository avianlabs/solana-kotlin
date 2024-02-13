package net.avianlabs.solana.domain.program

import net.avianlabs.solana.domain.core.AccountMeta
import net.avianlabs.solana.domain.core.PublicKey
import net.avianlabs.solana.domain.core.TransactionInstruction
import net.avianlabs.solana.domain.program.Program.Companion.createTransactionInstruction
import okio.Buffer

public object SystemProgram : Program {

  public override val programId: PublicKey =
    PublicKey.fromBase58("11111111111111111111111111111111")

  public val SYSVAR_RENT_ACCOUNT: PublicKey =
    PublicKey.fromBase58("SysvarRent111111111111111111111111111111111")

  public val SYSVAR_RECENT_BLOCKHASH: PublicKey =
    PublicKey.fromBase58("SysvarRecentB1ockHashes11111111111111111111")

  public val NONCE_ACCOUNT_LENGTH: Long = 80L

  public enum class Instruction(
    public val index: UInt,
  ) {
    CreateAccount(0u),
    Assign(1u),
    Transfer(2u),
    CreateAccountWithSeed(3u),
    AdvanceNonceAccount(4u),
    WithdrawNonceAccount(5u),
    InitializeNonceAccount(6u),
    AuthorizeNonceAccount(7u),
    Allocate(8u),
    AllocateWithSeed(9u),
    AssignWithSeed(10u),
    TransferWithSeed(11u),
    AdvanceNonceAccountWithSeed(12u),
    Push(13u),
    Pop(14u),
    Invoke(15u),
    Print(16u),
    Halt(17u),
    ;

  }

  public fun createAccount(
    fromPublicKey: PublicKey,
    newAccountPublicKey: PublicKey,
    lamports: Long,
    space: Long,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(fromPublicKey, isSigner = true, isWritable = true),
      AccountMeta(newAccountPublicKey, isSigner = true, isWritable = true),
    ),
    data = Buffer()
      .writeIntLe(Instruction.CreateAccount.index.toInt())
      .writeLongLe(lamports)
      .writeLongLe(space)
      .write(programId.bytes)
      .readByteArray(),
  )

  public fun transfer(
    fromPublicKey: PublicKey,
    toPublicKey: PublicKey,
    lamports: Long,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(fromPublicKey, isSigner = true, isWritable = true),
      AccountMeta(toPublicKey, isSigner = false, isWritable = true),
    ),
    data = Buffer()
      .writeIntLe(Instruction.Transfer.index.toInt())
      .writeLongLe(lamports)
      .readByteArray(),
  )

  public fun nonceInitialize(
    nonceAccount: PublicKey,
    authorized: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(nonceAccount, isSigner = false, isWritable = true),
      AccountMeta(SYSVAR_RECENT_BLOCKHASH, isSigner = false, isWritable = false),
      AccountMeta(SYSVAR_RENT_ACCOUNT, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeIntLe(Instruction.InitializeNonceAccount.index.toInt())
      .write(authorized.bytes)
      .readByteArray(),
  )

  public fun nonceAdvance(
    nonceAccount: PublicKey,
    authorized: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(nonceAccount, isSigner = false, isWritable = true),
      AccountMeta(SYSVAR_RECENT_BLOCKHASH, isSigner = false, isWritable = false),
      AccountMeta(authorized, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeIntLe(Instruction.AdvanceNonceAccount.index.toInt())
      .readByteArray(),
  )

  public fun nonceWithdraw(
    nonceAccount: PublicKey,
    authorized: PublicKey,
    toPublicKey: PublicKey,
    lamports: Long,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(nonceAccount, isSigner = false, isWritable = true),
      AccountMeta(toPublicKey, isSigner = false, isWritable = true),
      AccountMeta(SYSVAR_RECENT_BLOCKHASH, isSigner = false, isWritable = false),
      AccountMeta(SYSVAR_RENT_ACCOUNT, isSigner = false, isWritable = false),
      AccountMeta(authorized, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeIntLe(Instruction.WithdrawNonceAccount.index.toInt())
      .writeLongLe(lamports)
      .readByteArray(),
  )

  public fun nonceAuthorize(
    nonceAccount: PublicKey,
    authorized: PublicKey,
    newAuthorized: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(nonceAccount, isSigner = false, isWritable = true),
      AccountMeta(authorized, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeIntLe(Instruction.AuthorizeNonceAccount.index.toInt())
      .write(newAuthorized.bytes)
      .readByteArray(),
  )
}
