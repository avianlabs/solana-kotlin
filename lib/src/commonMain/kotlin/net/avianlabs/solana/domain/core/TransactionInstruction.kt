package net.avianlabs.solana.domain.core

public data class TransactionInstruction(
  val programId: PublicKey,
  val keys: List<AccountMeta>,
  val data: ByteArray,
) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as TransactionInstruction

    if (programId != other.programId) return false
    if (keys != other.keys) return false
    return data.contentEquals(other.data)
  }

  override fun hashCode(): Int {
    var result = programId.hashCode()
    result = 31 * result + keys.hashCode()
    result = 31 * result + data.contentHashCode()
    return result
  }
}
