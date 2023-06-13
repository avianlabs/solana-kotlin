package net.avianlabs.solana.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.komputing.kbase58.decodeBase58
import org.komputing.kbase58.encodeToBase58String
import kotlin.jvm.JvmInline

@JvmInline
@Serializable(with = PublicKeySerializer::class)
public value class PublicKey(public val bytes: ByteArray) {

  public fun toBase58(): String = bytes.encodeToBase58String()

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
