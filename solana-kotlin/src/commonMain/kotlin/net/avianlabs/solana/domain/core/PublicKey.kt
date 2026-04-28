package net.avianlabs.solana.domain.core

import kotlinx.io.Source
import kotlinx.io.readByteArray
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

internal fun PublicKey.Companion.read(data: Source): PublicKey = PublicKey(
  bytes = data.readByteArray(32),
)
