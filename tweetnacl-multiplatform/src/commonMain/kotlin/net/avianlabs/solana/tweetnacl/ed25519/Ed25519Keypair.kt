package net.avianlabs.solana.tweetnacl.ed25519

import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.vendor.decodeBase58

public data class Ed25519Keypair(
  public val publicKey: PublicKey,
  public val secretKey: ByteArray,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as Ed25519Keypair

    if (publicKey != other.publicKey) return false
    return secretKey.contentEquals(other.secretKey)
  }

  override fun hashCode(): Int {
    var result = publicKey.hashCode()
    result = 31 * result + secretKey.contentHashCode()
    return result
  }

  public override fun toString(): String = "Ed25519Keypair(publicKey=$publicKey, secretKey=*****)"

  public fun sign(message: ByteArray): ByteArray =
    TweetNaCl.Signature.sign(message = message, secretKey = secretKey)

  public companion object {
    public fun fromSecretKeyBytes(bytes: ByteArray): Ed25519Keypair = when (bytes.size) {
      // [secretKey(32)]
      32 -> {
        val publicKeyBytes = generatePublicKeyBytes(bytes)
        Ed25519Keypair(PublicKey(publicKeyBytes), bytes + publicKeyBytes)
      }
      // [secretKey(32)|publicKey(32)]
      64 -> {
        val publicKey = PublicKey(bytes.sliceArray(32 until 64))
        Ed25519Keypair(publicKey, bytes.copyOf())
      }

      else -> error("Invalid key length: ${bytes.size}")
    }

    public fun fromBase58(base58: String): Ed25519Keypair =
      fromSecretKeyBytes(base58.decodeBase58())
  }
}

internal expect fun generatePublicKeyBytes(secretKey: ByteArray): ByteArray
