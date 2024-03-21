package net.avianlabs.solana.domain.core

import kotlinx.serialization.Serializable
import okio.BufferedSource

@Serializable
public data class FeeCalculator(
  val lamportsPerSignature: ULong,
) {
  public companion object {
    public fun read(data: BufferedSource): FeeCalculator = FeeCalculator(
      lamportsPerSignature = data.readLongLe().toULong(),
    )
  }
}
