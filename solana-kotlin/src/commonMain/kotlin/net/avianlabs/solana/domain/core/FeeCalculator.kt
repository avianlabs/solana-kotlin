package net.avianlabs.solana.domain.core

import kotlinx.io.Source
import kotlinx.io.readLongLe
import kotlinx.serialization.Serializable

@Serializable
public data class FeeCalculator(
  val lamportsPerSignature: ULong,
) {
  public companion object {
    internal fun read(data: Source): FeeCalculator = FeeCalculator(
      lamportsPerSignature = data.readLongLe().toULong(),
    )
  }
}
