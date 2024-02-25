package net.avianlabs.solana.crypto

internal interface CryptoEngine {
  fun sign(message: ByteArray, secretKey: ByteArray): ByteArray
  fun isOnCurve(publicKey: ByteArray): Boolean
  fun generateKey(seed: ByteArray = defaultSecureRandom.randomBytes(Ed25519Keypair.SECRET_BYTES)): Ed25519Keypair
}

internal expect val defaultCryptoEngine: CryptoEngine
