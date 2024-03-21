package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import okio.BufferedSource

public data class NonceAccountData(
  val version: UInt,
  val state: UInt,
  val authorizedPubkey: PublicKey,
  val nonce: String,
  val feeCalculator: FeeCalculator,
) {
  public companion object {
    public fun read(data: BufferedSource): NonceAccountData {
      val version = data.readIntLe().toUInt()
      val state = data.readIntLe().toUInt()
      val authorizedPubkey = PublicKey.read(data)
      val nonce = PublicKey.read(data).toBase58()
      val feeCalculator = FeeCalculator.read(data)
      return NonceAccountData(
        version = version,
        state = state,
        authorizedPubkey = authorizedPubkey,
        nonce = nonce,
        feeCalculator = feeCalculator,
      )
    }
  }
}
