package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.crypto.PublicKey

public data class SignaturePubkeyPair(
  val signature: String,
  val publicKey: PublicKey,
)
