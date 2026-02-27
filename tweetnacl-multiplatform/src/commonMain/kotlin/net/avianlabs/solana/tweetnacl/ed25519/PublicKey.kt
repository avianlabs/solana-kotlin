package net.avianlabs.solana.tweetnacl.ed25519

import co.touchlab.skie.configuration.annotations.SkieVisibility
import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.TweetNaCl.Signature.Companion.PUBLIC_KEY_BYTES
import net.avianlabs.solana.tweetnacl.vendor.decodeBase58
import net.avianlabs.solana.tweetnacl.vendor.encodeToBase58String

public data class PublicKey(
  @property:SkieVisibility.PublicButReplaced public val bytes: ByteArray,
) {

  init {
    require(bytes.size == PUBLIC_KEY_BYTES) { "Invalid public key input size ${bytes.size} (must be $PUBLIC_KEY_BYTES)" }
  }

  public fun toBase58(): String = bytes.encodeToBase58String()

  override fun toString(): String = toBase58()

  public fun toByteArray(): ByteArray = bytes

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as PublicKey

    return bytes.contentEquals(other.bytes)
  }

  override fun hashCode(): Int = bytes.contentHashCode()

  public fun isOnCurve(): Boolean = TweetNaCl.Signature.isOnCurve(this.bytes)

  public companion object {
    public fun fromBase58(base58: String): PublicKey = PublicKey(base58.decodeBase58())
  }
}
