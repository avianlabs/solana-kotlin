package net.avianlabs.solana.tweetnacl.ed25519

import kotlinx.cinterop.*
import platform.Foundation.NSData

public fun PublicKey(data: NSData): PublicKey = PublicKey(
  bytes = data.toKotlinByteArray(),
)

public val PublicKey.data: NSData
  get() = bytes.toNSData()
