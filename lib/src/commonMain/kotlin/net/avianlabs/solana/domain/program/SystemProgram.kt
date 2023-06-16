package net.avianlabs.solana.domain.program

import net.avianlabs.solana.domain.core.AccountMeta
import net.avianlabs.solana.domain.core.PublicKey
import net.avianlabs.solana.domain.core.TransactionInstruction
import okio.Buffer

private val SYSTEM_PROGRAM_ID = PublicKey.fromBase58("11111111111111111111111111111111")

public object SystemProgram : Program(
  programId = SYSTEM_PROGRAM_ID
) {
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
}
