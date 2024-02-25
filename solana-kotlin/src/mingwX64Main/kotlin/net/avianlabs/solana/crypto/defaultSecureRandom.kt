package net.avianlabs.solana.crypto

import kotlin.random.Random

internal actual val defaultSecureRandom: SecureRandom = object : SecureRandom {
  override fun randomBytes(length: Int): ByteArray = Random.nextBytes(length)
}
