@file:Suppress("DEPRECATION")

package net.avianlabs.solana.domain.core

import kotlinx.cinterop.*
import net.avianlabs.solana.tweetnacl.ed25519.toNSData
import platform.Foundation.NSData

public fun VersionedTransaction.serializeData(): NSData = serialize().toByteArray().toNSData()

@Deprecated(
  message = "Use VersionedTransaction.serializeData() instead",
  replaceWith = ReplaceWith("serializeData()"),
)
public fun SignedTransaction.serializeData(): NSData = serialize().toByteArray().toNSData()
