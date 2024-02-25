package net.avianlabs.solana.crypto

internal actual val defaultSecureRandom: SecureRandom = object : SecureRandom {
  private val secureRandom = java.security.SecureRandom.getInstanceStrong()

  override fun randomBytes(length: Int): ByteArray = secureRandom.generateSeed(length)
}
