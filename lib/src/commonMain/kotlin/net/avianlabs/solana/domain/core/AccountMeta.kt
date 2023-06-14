package net.avianlabs.solana.domain.core

import kotlinx.serialization.Serializable

@Serializable
public data class AccountMeta(
  val publicKey: PublicKey,
  val isSigner: Boolean,
  val isWritable: Boolean,
)
