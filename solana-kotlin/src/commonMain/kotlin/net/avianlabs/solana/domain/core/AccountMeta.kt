package net.avianlabs.solana.domain.core

public data class AccountMeta(
  val publicKey: PublicKey,
  val isSigner: Boolean,
  val isWritable: Boolean,
)
