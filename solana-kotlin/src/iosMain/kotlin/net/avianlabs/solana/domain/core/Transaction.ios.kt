package net.avianlabs.solana.domain.core

import kotlinx.cinterop.*
import net.avianlabs.solana.tweetnacl.ed25519.toNSData
import platform.Foundation.NSData

public fun SignedTransaction.serializeData(): NSData = serialize().toNSData()
