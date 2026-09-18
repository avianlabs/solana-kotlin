package net.avianlabs.solana.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

internal class PublicKeyBase58Serializer : KSerializer<PublicKey> {

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
