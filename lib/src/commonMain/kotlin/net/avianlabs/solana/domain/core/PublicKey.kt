package net.avianlabs.solana.domain.core

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

  init {
    require(bytes.size == PUBLIC_KEY_LENGTH) { "Invalid public key input size ${bytes.size} (must be $PUBLIC_KEY_LENGTH)" }
  }

  public fun toBase58(): String = bytes.encodeToBase58String()

  override fun toString(): String = toBase58()

  public companion object {

    public const val PUBLIC_KEY_LENGTH: Int = 32

//    public fun createProgramAddress(seeds: List<ByteArray>, programId: PublicKey): PublicKey {
//      val bytes = (seeds + programId.bytes + "ProgramDerivedAddress".toByteArray()).let {
//        // join into one single byte array
//        val buffer = ByteArray(it.sumOf { it.size })
//        var offset = 0
//        for (seed in it) {
//          seed.copyInto(buffer, offset)
//          offset += seed.size
//        }
//        buffer
//      }
//      val hash = Sha256.digest(bytes)
//
//      if (TweetNaclFast.is_on_curve(hash) != 0) {
//        throw RuntimeException("Invalid seeds, address must fall off the curve")
//      }
//      return PublicKey(hash)
//    }

//    public fun findProgramAddress(
//      seeds: List<ByteArray>,
//      programId: PublicKey,
//    ): ProgramDerivedAddress {
//      var nonce = 255
//      val address: PublicKey
//      val seedsWithNonce = mutableListOf<ByteArray>()
//      seedsWithNonce.addAll(seeds)
//      while (nonce != 0) {
//        address = try {
//          seedsWithNonce.add(byteArrayOf(nonce.toByte()))
//          createProgramAddress(seedsWithNonce, programId)
//        } catch (e: Exception) {
//          seedsWithNonce.removeAt(seedsWithNonce.size - 1)
//          nonce--
//          continue
//        }
//        return ProgramDerivedAddress(address, nonce)
//      }
//      throw Exception("Unable to find a viable program address nonce")
//    }

//    @Throws(Exception::class)
//    public fun associatedTokenAddress(
//      walletAddress: PublicKey,
//      tokenMintAddress: PublicKey,
//    ): ProgramDerivedAddress {
//      return findProgramAddress(
//        listOf(
//          walletAddress.bytes,
//          TokenProgram.PROGRAM_ID.toByteArray(),
//          tokenMintAddress.toByteArray()
//        ),
//        fromBase58("ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL")
//      )
//    }

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
