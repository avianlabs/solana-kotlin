@file:Suppress("DEPRECATION")

package net.avianlabs.solana.domain.core

import io.ktor.util.*
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.vendor.ShortVecEncoding
import kotlin.jvm.JvmInline

@JvmInline
public value class SerializedTransaction(private val bytes: ByteArray) {

  public fun toByteArray(): ByteArray = bytes

  override fun toString(): String = bytes.encodeBase64()

  public fun sign(signer: Signer): SerializedTransaction =
    sign(listOf(signer))

  public fun sign(signers: List<Signer>): SerializedTransaction =
    toVersionedTransaction().sign(signers).serialize()

  /**
   * Deserializes this transaction into a [VersionedTransaction].
   */
  public fun toVersionedTransaction(): VersionedTransaction =
    VersionedTransaction.deserialize(bytes)

  @Deprecated(
    message = "Use toVersionedTransaction() instead",
    replaceWith = ReplaceWith("toVersionedTransaction()"),
  )
  public fun toSignedTransaction(): SignedTransaction {
    val source = Buffer().apply { write(bytes) }

    val numSignatures = ShortVecEncoding.decodeLength(source)
    val existingSignatures = Array(numSignatures) {
      source.readByteArray(TweetNaCl.Signature.SIGNATURE_BYTES)
    }
    val messageBytes = source.readByteArray()

    val message = VersionedMessage.deserialize(messageBytes)
    val signerKeys = message.staticAccountKeys
      .filter { it.isSigner }
      .map { it.publicKey }

    val signatures = signerKeys.zip(existingSignatures.toList()).toMap()

    return SignedTransaction(
      message = message,
      serializedMessage = messageBytes,
      signatures = signatures,
      signerKeys = signerKeys,
    )
  }
}
