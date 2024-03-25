package net.avianlabs.solana.domain.program

import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

public data class ProgramDerivedAddress(
  val address: PublicKey,
  val nonce: UByte,
)
