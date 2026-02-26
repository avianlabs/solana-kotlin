package net.avianlabs.solana.domain.core

import io.ktor.util.*
import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import net.avianlabs.solana.vendor.ShortVecEncoding
import okio.Buffer
import kotlin.jvm.JvmInline

@JvmInline
public value class SerializedTransaction(private val bytes: ByteArray) {

  public fun toByteArray(): ByteArray = bytes

  override fun toString(): String = bytes.encodeBase64()

  public fun sign(signer: Signer): SerializedTransaction =
    sign(listOf(signer))

  public fun sign(signers: List<Signer>): SerializedTransaction =
    toSignedTransaction().sign(signers).serialize()

  public fun toSignedTransaction(): SignedTransaction {
    val source = Buffer().apply { write(bytes) }

    // 1. Read compact-u16 → number of existing signatures
    val numSignatures = ShortVecEncoding.decodeLength(source)

    // 2. Read existing 64-byte signatures
    val existingSignatures = Array(numSignatures) {
      source.readByteArray(TweetNaCl.Signature.SIGNATURE_BYTES.toLong())
    }

    // 3. Remaining bytes → messageBytes
    val messageBytes = source.readByteArray()

    // 4. Parse message header: numRequiredSignatures (byte 0)
    val numRequiredSignatures = messageBytes[0].toInt() and 0xFF

    // 5. Skip 2 header bytes (readonly signed + readonly unsigned),
    //    skip compact-u16 account count
    val messageSource = Buffer().apply {
      write(messageBytes, 3, messageBytes.size - 3)
    }
    ShortVecEncoding.decodeLength(messageSource)

    // 6. Read first numRequiredSignatures × 32-byte keys → signerKeys
    val signerKeys = (0 until numRequiredSignatures).map {
      PublicKey(messageSource.readByteArray(TweetNaCl.Signature.PUBLIC_KEY_BYTES.toLong()))
    }

    val signatures = signerKeys.zip(existingSignatures.toList()).toMap()

    return SignedTransaction(
      serializedMessage = messageBytes,
      signatures = signatures,
      signerKeys = signerKeys,
    )
  }
}
