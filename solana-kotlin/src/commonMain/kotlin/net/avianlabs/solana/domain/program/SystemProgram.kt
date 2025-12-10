package net.avianlabs.solana.domain.program

import kotlin.Any
import kotlin.Deprecated
import kotlin.Long
import kotlin.UInt
import kotlin.ULong
import net.avianlabs.solana.domain.core.AccountMeta
import net.avianlabs.solana.domain.core.TransactionInstruction
import net.avianlabs.solana.domain.program.Program.Companion.createTransactionInstruction
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import okio.Buffer

public object SystemProgram : Program {
  public override val programId: PublicKey =
      PublicKey.fromBase58("11111111111111111111111111111111")

  public val RECENT_BLOCKHASHES_SYSVAR: PublicKey =
      PublicKey.fromBase58("SysvarRecentB1ockHashes11111111111111111111")

  public val RENT_SYSVAR: PublicKey =
      PublicKey.fromBase58("SysvarRent111111111111111111111111111111111")

  public val NONCE_LENGTH: Long = 80L

  public fun createAccount(
    newAccount: PublicKey,
    lamports: ULong,
    space: ULong,
    programAddress: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(newAccount, isSigner = true, isWritable = true),
    ),
    data = Buffer()
      .writeIntLe(Instruction.CreateAccount.index.toInt())
      .writeLongLe(lamports.toLong())
      .writeLongLe(space.toLong())
      .write(programAddress.bytes)
    .readByteArray(),
  )

  public fun assign(account: PublicKey, programAddress: PublicKey): TransactionInstruction =
      createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = true, isWritable = true),
    ),
    data = Buffer()
      .writeIntLe(Instruction.Assign.index.toInt())
      .write(programAddress.bytes)
    .readByteArray(),
  )

  public fun transferSol(
    source: PublicKey,
    destination: PublicKey,
    amount: ULong,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(source, isSigner = true, isWritable = true),
      AccountMeta(destination, isSigner = false, isWritable = true),
    ),
    data = Buffer()
      .writeIntLe(Instruction.TransferSol.index.toInt())
      .writeLongLe(amount.toLong())
    .readByteArray(),
  )

  @Deprecated(
    message = "Use transferSol instead",
    replaceWith =
        ReplaceWith("transferSol(fromPublicKey = fromPublicKey, toPublicKey = toPublicKey, amount = lamports)"),
  )
  public fun transfer(
    fromPublicKey: PublicKey,
    toPublicKey: PublicKey,
    lamports: Long,
  ): TransactionInstruction = transferSol(fromPublicKey, toPublicKey, lamports.toULong())

  public fun createAccountWithSeed(
    newAccount: PublicKey,
    baseAccount: PublicKey,
    base: PublicKey,
    seed: Any,
    amount: ULong,
    space: ULong,
    programAddress: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(newAccount, isSigner = false, isWritable = true),
      AccountMeta(baseAccount, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeIntLe(Instruction.CreateAccountWithSeed.index.toInt())
      .write(base.bytes)
      .writeLongLe(amount.toLong())
      .writeLongLe(space.toLong())
      .write(programAddress.bytes)
    .readByteArray(),
  )

  public fun advanceNonceAccount(nonceAccount: PublicKey, nonceAuthority: PublicKey):
      TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(nonceAccount, isSigner = false, isWritable = true),
      AccountMeta(RECENT_BLOCKHASHES_SYSVAR, isSigner = false, isWritable = false),
      AccountMeta(nonceAuthority, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeIntLe(Instruction.AdvanceNonceAccount.index.toInt())
    .readByteArray(),
  )

  public fun withdrawNonceAccount(
    nonceAccount: PublicKey,
    recipientAccount: PublicKey,
    nonceAuthority: PublicKey,
    withdrawAmount: ULong,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(nonceAccount, isSigner = false, isWritable = true),
      AccountMeta(recipientAccount, isSigner = false, isWritable = true),
      AccountMeta(RECENT_BLOCKHASHES_SYSVAR, isSigner = false, isWritable = false),
      AccountMeta(RENT_SYSVAR, isSigner = false, isWritable = false),
      AccountMeta(nonceAuthority, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeIntLe(Instruction.WithdrawNonceAccount.index.toInt())
      .writeLongLe(withdrawAmount.toLong())
    .readByteArray(),
  )

  public fun initializeNonceAccount(nonceAccount: PublicKey, nonceAuthority: PublicKey):
      TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(nonceAccount, isSigner = false, isWritable = true),
      AccountMeta(RECENT_BLOCKHASHES_SYSVAR, isSigner = false, isWritable = false),
      AccountMeta(RENT_SYSVAR, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeIntLe(Instruction.InitializeNonceAccount.index.toInt())
      .write(nonceAuthority.bytes)
    .readByteArray(),
  )

  public fun authorizeNonceAccount(
    nonceAccount: PublicKey,
    nonceAuthority: PublicKey,
    newNonceAuthority: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(nonceAccount, isSigner = false, isWritable = true),
      AccountMeta(nonceAuthority, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeIntLe(Instruction.AuthorizeNonceAccount.index.toInt())
      .write(newNonceAuthority.bytes)
    .readByteArray(),
  )

  public fun allocate(newAccount: PublicKey, space: ULong): TransactionInstruction =
      createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(newAccount, isSigner = true, isWritable = true),
    ),
    data = Buffer()
      .writeIntLe(Instruction.Allocate.index.toInt())
      .writeLongLe(space.toLong())
    .readByteArray(),
  )

  public fun allocateWithSeed(
    newAccount: PublicKey,
    baseAccount: PublicKey,
    base: PublicKey,
    seed: Any,
    space: ULong,
    programAddress: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(newAccount, isSigner = false, isWritable = true),
      AccountMeta(baseAccount, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeIntLe(Instruction.AllocateWithSeed.index.toInt())
      .write(base.bytes)
      .writeLongLe(space.toLong())
      .write(programAddress.bytes)
    .readByteArray(),
  )

  public fun assignWithSeed(
    account: PublicKey,
    baseAccount: PublicKey,
    base: PublicKey,
    seed: Any,
    programAddress: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
      AccountMeta(baseAccount, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeIntLe(Instruction.AssignWithSeed.index.toInt())
      .write(base.bytes)
      .write(programAddress.bytes)
    .readByteArray(),
  )

  public fun transferSolWithSeed(
    source: PublicKey,
    baseAccount: PublicKey,
    destination: PublicKey,
    amount: ULong,
    fromSeed: Any,
    fromOwner: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(source, isSigner = false, isWritable = true),
      AccountMeta(baseAccount, isSigner = true, isWritable = false),
      AccountMeta(destination, isSigner = false, isWritable = true),
    ),
    data = Buffer()
      .writeIntLe(Instruction.TransferSolWithSeed.index.toInt())
      .writeLongLe(amount.toLong())
      .write(fromOwner.bytes)
    .readByteArray(),
  )

  public fun upgradeNonceAccount(nonceAccount: PublicKey): TransactionInstruction =
      createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(nonceAccount, isSigner = false, isWritable = true),
    ),
    data = Buffer()
      .writeIntLe(Instruction.UpgradeNonceAccount.index.toInt())
    .readByteArray(),
  )

  public enum class Instruction(
    public val index: UInt,
  ) {
    CreateAccount(0u),
    Assign(1u),
    TransferSol(2u),
    CreateAccountWithSeed(3u),
    AdvanceNonceAccount(4u),
    WithdrawNonceAccount(5u),
    InitializeNonceAccount(6u),
    AuthorizeNonceAccount(7u),
    Allocate(8u),
    AllocateWithSeed(9u),
    AssignWithSeed(10u),
    TransferSolWithSeed(11u),
    UpgradeNonceAccount(12u),
    ;
  }
}
