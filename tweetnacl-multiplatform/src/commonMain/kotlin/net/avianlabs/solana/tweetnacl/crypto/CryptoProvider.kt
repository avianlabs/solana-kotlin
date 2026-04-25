package net.avianlabs.solana.tweetnacl.crypto

import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.ed25519.Ed25519Keypair

internal interface CryptoProvider {
  fun sign(message: ByteArray, secretKey: ByteArray): ByteArray
  fun generateKey(seed: ByteArray): Ed25519Keypair
  fun isOnCurve(publicKey: ByteArray): Boolean
  fun secretBox(secretKey: ByteArray): TweetNaCl.SecretBox
  fun secureRandomBytes(count: Int): ByteArray
}

internal expect fun platformCryptoProvider(): CryptoProvider

internal val DefaultCryptoProvider: CryptoProvider by lazy { platformCryptoProvider() }
