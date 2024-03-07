package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

public data class AccountMeta(
  val publicKey: PublicKey,
  val isSigner: Boolean,
  val isWritable: Boolean,
)
