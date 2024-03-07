package net.avianlabs.solana.tweetnacl

import net.avianlabs.solana.tweetnacl.ed25519.Ed25519Keypair

public interface TweetNaCl {
  public interface Signature {
    public fun sign(message: ByteArray, secretKey: ByteArray): ByteArray
    public fun generateKey(seed: ByteArray): Ed25519Keypair
    public fun isOnCurve(publicKey: ByteArray): Boolean

    public companion object : Signature {
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
