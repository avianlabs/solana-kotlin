package net.avianlabs.solana.tweetnacl

import java.security.SecureRandom

private val secureRandom = SecureRandom()

public actual fun secureRandomBytes(count: Int): ByteArray =
  ByteArray(count).also { secureRandom.nextBytes(it) }
