package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.crypto.PublicKey

public data class AccountMeta(
  val publicKey: PublicKey,
  val isSigner: Boolean,
  val isWritable: Boolean,
)
