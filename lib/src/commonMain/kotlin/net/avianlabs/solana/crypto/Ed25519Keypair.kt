package net.avianlabs.solana.crypto

import net.avianlabs.solana.domain.core.PublicKey

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

  public companion object {
    public fun fromSecretKeyBytes(bytes: ByteArray): Ed25519Keypair {
      require(bytes.size == 64) { "Invalid key length" }
      val publicKey = PublicKey(bytes.sliceArray(32 until 64))
      return Ed25519Keypair(publicKey, bytes.copyOf())
    }
  }
}

public fun Ed25519Keypair.sign(message: ByteArray): ByteArray =
  defaultCryptoEngine.sign(message = message, secretKey = secretKey)
