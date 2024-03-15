package net.avianlabs.solana.tweetnacl.ed25519

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.TweetNaCl.Signature.Companion.PUBLIC_KEY_BYTES
import net.avianlabs.solana.tweetnacl.vendor.decodeBase58
import net.avianlabs.solana.tweetnacl.vendor.encodeToBase58String

@Serializable(with = PublicKeySerializer::class)
public data class PublicKey(public val bytes: ByteArray) {

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

private class PublicKeySerializer : KSerializer<PublicKey> {

  private val delegateSerializer = String.serializer()

  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("PublicKey", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: PublicKey) = with(encoder) {
    encodeSerializableValue(delegateSerializer, value.toBase58())
  }

  override fun deserialize(decoder: Decoder): PublicKey = PublicKey.fromBase58(
    decoder.decodeSerializableValue(delegateSerializer),
  )
}
