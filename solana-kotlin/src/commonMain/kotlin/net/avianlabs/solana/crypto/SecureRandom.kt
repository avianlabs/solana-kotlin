package net.avianlabs.solana.crypto

internal interface SecureRandom {
  fun randomBytes(length: Int): ByteArray
}

internal expect val defaultSecureRandom: SecureRandom
