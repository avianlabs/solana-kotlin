package net.avianlabs.solana.tweetnacl.ed25519

import platform.Foundation.NSData

public fun Ed25519Keypair.Companion.fromSecretKeyData(data: NSData): Ed25519Keypair =
  fromSecretKeyBytes(data.toKotlinByteArray())

public val Ed25519Keypair.secretKeyData: NSData
  get() = secretKey.toNSData()
