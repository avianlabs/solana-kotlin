package net.avianlabs.solana.domain.program

import net.avianlabs.solana.domain.core.PublicKey
import net.avianlabs.solana.domain.core.TransactionInstruction
import net.avianlabs.solana.domain.program.Program.Companion.createTransactionInstruction
import okio.Buffer


public object ComputeBudgetProgram : Program {

  public override val programId: PublicKey =
    PublicKey.fromBase58("ComputeBudget111111111111111111111111111111")

  public enum class Instruction(
    public val index: UByte,
  ) {
    RequestUnits(0u),
    RequestHeapFrames(1u),
    SetComputeUnitLimit(2u),
    SetComputeUnitPrice(3u),
    ;
  }

  /**
   * @param microLamports Transaction compute unit price used for prioritization fees.
   */
  public fun setComputeUnitPrice(
    microLamports: ULong,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
    ),
    data = Buffer()
      .writeByte(Instruction.SetComputeUnitPrice.index.toInt())
      .writeLongLe(microLamports.toLong())
      .readByteArray(),
  )
}
