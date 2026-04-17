package net.avianlabs.solana.tweetnacl

/**
 * Returns [count] cryptographically secure random bytes.
 *
 * Each platform delegates to the OS-provided CSPRNG:
 * - JVM: `java.security.SecureRandom`
 * - Apple: `SecRandomCopyBytes`
 * - Linux: `/dev/urandom`
 * - Windows: `BCryptGenRandom`
 */
public expect fun secureRandomBytes(count: Int): ByteArray
