package net.avianlabs.solana.tweetnacl

import net.avianlabs.solana.tweetnacl.crypto.DefaultCryptoProvider

/**
 * Returns [count] cryptographically secure random bytes.
 *
 * Each platform delegates to the OS-provided CSPRNG:
 * - JVM / Android: `java.security.SecureRandom`
 * - Apple: `SecRandomCopyBytes`
 * - Linux: `/dev/urandom`
 * - Windows: `BCryptGenRandom`
 */
public fun secureRandomBytes(count: Int): ByteArray = DefaultCryptoProvider.secureRandomBytes(count)
