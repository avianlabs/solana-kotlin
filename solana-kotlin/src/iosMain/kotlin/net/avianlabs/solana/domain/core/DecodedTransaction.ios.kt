package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.ed25519.toNSData
import platform.Foundation.NSData

public val SignaturePublicKeyPair.signatureData: NSData?
  get() = signature?.toNSData()
