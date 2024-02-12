package net.avianlabs.solana.domain.program

import net.avianlabs.solana.domain.core.PublicKey
import net.avianlabs.solana.domain.core.TransactionInstruction
import okio.Buffer

private val COMPUTE_BUDGET_PROGRAM_ID =
  PublicKey.fromBase58("ComputeBudget111111111111111111111111111111")

public object ComputeBudgetProgram : Program(
  programId = COMPUTE_BUDGET_PROGRAM_ID,
) {

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
