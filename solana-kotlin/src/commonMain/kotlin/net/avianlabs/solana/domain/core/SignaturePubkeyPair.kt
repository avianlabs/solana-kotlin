package net.avianlabs.solana.domain.core

public data class SignaturePubkeyPair(
  val signature: String,
  val publicKey: PublicKey,
)
