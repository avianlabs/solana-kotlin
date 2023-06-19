package net.avianlabs.solana.domain.core

public data class CompiledInstruction(
  /**
   * Index into the transaction keys array indicating the program account that executes this instruction
   */
  val programIdIndex: Byte = 0,

  /**
   * Ordered indices into the transaction keys array indicating which accounts to pass to the program
   */
  val accounts: List<Byte>,

  /**
   * The program input data
   */
  val data: ByteArray,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as CompiledInstruction

    if (programIdIndex != other.programIdIndex) return false
    if (accounts != other.accounts) return false
    return data.contentEquals(other.data)
  }

  override fun hashCode(): Int {
    var result = programIdIndex.toInt()
    result = 31 * result + accounts.hashCode()
    result = 31 * result + data.contentHashCode()
    return result
  }
}
