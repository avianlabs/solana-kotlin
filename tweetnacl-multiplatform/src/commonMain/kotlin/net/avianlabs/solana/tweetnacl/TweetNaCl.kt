package net.avianlabs.solana.tweetnacl

import net.avianlabs.solana.tweetnacl.ed25519.Ed25519Keypair

public interface TweetNaCl {
  public interface Signature {

    /**
     * Signs the message using the secret key and returns a signed message
     *
     * @param message:
     * @param secretKey:
     */
    public fun sign(message: ByteArray, secretKey: ByteArray): ByteArray

    /**
     * Returns a new signing key pair generated deterministically from a seed
     *
     * @param seed: 32 byte seed. Must contain enough entropy to be secure.
     */
    public fun generateKey(seed: ByteArray): Ed25519Keypair

    /**
     * Returns whether the given publicKey falls in the Ed25519 elliptic curve
     */
    public fun isOnCurve(publicKey: ByteArray): Boolean

    public companion object : Signature {
      public const val SECRET_KEY_BYTES: Int = 64
      public const val PUBLIC_KEY_BYTES: Int = 32

      override fun sign(message: ByteArray, secretKey: ByteArray): ByteArray =
        signInternal(message, secretKey)

      override fun generateKey(seed: ByteArray): Ed25519Keypair =
        generateKeyInternal(seed)

      override fun isOnCurve(publicKey: ByteArray): Boolean =
        isOnCurveInternal(publicKey)
    }
  }

}

internal expect fun signInternal(message: ByteArray, secretKey: ByteArray): ByteArray
internal expect fun isOnCurveInternal(publicKey: ByteArray): Boolean
internal expect fun generateKeyInternal(seed: ByteArray): Ed25519Keypair
