package net.avianlabs.solana.crypto

internal interface CryptoEngine {
  fun sign(message: ByteArray, secretKey: ByteArray): ByteArray
  fun isOnCurve(publicKey: ByteArray): Boolean
}

internal expect val defaultCryptoEngine: CryptoEngine
