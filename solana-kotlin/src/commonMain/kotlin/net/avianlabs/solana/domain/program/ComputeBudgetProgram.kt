package net.avianlabs.solana.domain.program

import kotlin.UByte
import kotlin.UInt
import kotlin.ULong
import net.avianlabs.solana.domain.core.AccountMeta
import net.avianlabs.solana.domain.core.TransactionInstruction
import net.avianlabs.solana.domain.program.Program.Companion.createTransactionInstruction
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import okio.Buffer

public object ComputeBudgetProgram : Program {
  public override val programId: PublicKey =
      PublicKey.fromBase58("ComputeBudget111111111111111111111111111111")

  public fun requestUnits(units: UInt, additionalFee: UInt): TransactionInstruction =
      createTransactionInstruction(
    programId = programId,
    keys = listOf(
    ),
    data = Buffer()
      .writeByte(Instruction.RequestUnits.index.toInt())
      .writeIntLe(units.toInt())
      .writeIntLe(additionalFee.toInt())
    .readByteArray(),
  )

  public fun requestHeapFrame(bytes: UInt): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
    ),
    data = Buffer()
      .writeByte(Instruction.RequestHeapFrame.index.toInt())
      .writeIntLe(bytes.toInt())
    .readByteArray(),
  )

  public fun setComputeUnitLimit(units: UInt): TransactionInstruction =
      createTransactionInstruction(
    programId = programId,
    keys = listOf(
    ),
    data = Buffer()
      .writeByte(Instruction.SetComputeUnitLimit.index.toInt())
      .writeIntLe(units.toInt())
    .readByteArray(),
  )

  public fun setComputeUnitPrice(microLamports: ULong): TransactionInstruction =
      createTransactionInstruction(
    programId = programId,
    keys = listOf(
    ),
    data = Buffer()
      .writeByte(Instruction.SetComputeUnitPrice.index.toInt())
      .writeLongLe(microLamports.toLong())
    .readByteArray(),
  )

  public fun setLoadedAccountsDataSizeLimit(accountDataSizeLimit: UInt): TransactionInstruction =
      createTransactionInstruction(
    programId = programId,
    keys = listOf(
    ),
    data = Buffer()
      .writeByte(Instruction.SetLoadedAccountsDataSizeLimit.index.toInt())
      .writeIntLe(accountDataSizeLimit.toInt())
    .readByteArray(),
  )

  public enum class Instruction(
    public val index: UByte,
  ) {
    RequestUnits(0u),
    RequestHeapFrame(1u),
    SetComputeUnitLimit(2u),
    SetComputeUnitPrice(3u),
    SetLoadedAccountsDataSizeLimit(4u),
    ;
  }
}
