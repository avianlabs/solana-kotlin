package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import okio.BufferedSource

public fun PublicKey.Companion.read(data: BufferedSource): PublicKey = PublicKey(
  bytes = data.readByteArray(32),
)
