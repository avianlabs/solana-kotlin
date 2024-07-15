package net.avianlabs.solana.tweetnacl

import net.avianlabs.solana.tweetnacl.ed25519.Ed25519Keypair

public interface TweetNaCl {

  /**
   * Implements the Ed25519 signature scheme
   *
   * @see <a href="https://ed25519.cr.yp.to/">Ed25519 spec</a>
   */
  public interface Signature {

    /**
     * Signs the message using the secret key and returns a signed message
     *
     * @param message: The message to sign
     * @param secretKey: The secret key to sign the message with
     * @return The signature of the message
     */
    public fun sign(message: ByteArray, secretKey: ByteArray): ByteArray

    /**
     * Returns a new signing key pair generated deterministically from a seed
     *
     * @param seed: 32 byte seed. Must contain enough entropy to be secure.
     * @return A new key pair generated from the seed
     */
    public fun generateKey(seed: ByteArray): Ed25519Keypair

    /**
     * Returns whether the given publicKey falls in the Ed25519 elliptic curve
     *
     * @param publicKey: The public key to check
     * @return Whether the public key is on the curve
     */
    public fun isOnCurve(publicKey: ByteArray): Boolean

    public companion object : Signature {
      public const val SEED_BYTES: Int = 32
      public const val SECRET_KEY_BYTES: Int = 64
      public const val PUBLIC_KEY_BYTES: Int = 32
      public const val SIGNATURE_BYTES: Int = 64

      override fun sign(message: ByteArray, secretKey: ByteArray): ByteArray =
        signInternal(message, secretKey)

      override fun generateKey(seed: ByteArray): Ed25519Keypair =
        generateKeyInternal(seed)

      override fun isOnCurve(publicKey: ByteArray): Boolean =
        isOnCurveInternal(publicKey)
    }
  }

  /**
   * Implements NaCl SecretBox (xsalsa20-poly1305)
   *
   * @see <a href="https://nacl.cr.yp.to/secretbox.html">SecretBox spec</a>
   */
  public interface SecretBox {

    /**
     * Encrypts and authenticates message using the key and the nonce. The nonce must be unique for each distinct message for this key.
     *
     * @param message: The message to encrypt
     * @param nonce: The nonce to use for encryption
     * @return The encrypted message
     */
    public fun box(message: ByteArray, nonce: ByteArray): ByteArray

    /**
     * Authenticates and decrypts the given secret box using the key and the nonce.
     *
     * @param box: The encrypted message
     * @param nonce: The nonce used for encryption
     * @return The decrypted message
     */
    public fun open(box: ByteArray, nonce: ByteArray): ByteArray

    public companion object {
      public const val NONCE_BYTES: Int = 24
      public const val KEY_BYTES: Int = 32

      public operator fun invoke(secretKey: ByteArray): SecretBox =
        secretBoxInternal(secretKey)
    }
  }
}

internal expect fun signInternal(message: ByteArray, secretKey: ByteArray): ByteArray
internal expect fun isOnCurveInternal(publicKey: ByteArray): Boolean
internal expect fun generateKeyInternal(seed: ByteArray): Ed25519Keypair

internal expect fun secretBoxInternal(secretKey: ByteArray): TweetNaCl.SecretBox
