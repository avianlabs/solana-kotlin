package net.avianlabs.solana.crypto

internal actual val defaultSecureRandom: SecureRandom = object: SecureRandom {
  override fun randomBytes(length: Int): ByteArray {
    TODO("Not yet implemented")
  }
}
