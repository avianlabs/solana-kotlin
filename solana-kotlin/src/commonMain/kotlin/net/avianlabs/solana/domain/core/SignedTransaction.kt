package net.avianlabs.solana.domain.core

import io.github.oshai.kotlinlogging.KotlinLogging
import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.vendor.decodeBase58
import net.avianlabs.solana.tweetnacl.vendor.encodeToBase58String
import net.avianlabs.solana.vendor.ShortvecEncoding
import okio.Buffer

private val logger = KotlinLogging.logger {}

public class SignedTransaction(
  public val originalMessage: Message,
  public val signedMessage: ByteArray,
  public val signatures: List<String>,
) : Transaction(originalMessage) {

  public override fun sign(signers: List<Signer>): SignedTransaction = SignedTransaction(
    originalMessage = originalMessage,
    signedMessage = signedMessage,
    signatures = signatures + signers.map { signer ->
      TweetNaCl.Signature.sign(signedMessage, signer.secretKey).encodeToBase58String()
    }
  ).also {
    val signatureSet = signatures.toSet()
    if (signatureSet.size != signatures.size) {
      logger.warn { "Duplicate signatures detected" }
    }
  }

  override fun toString(): String =
    "SignedTransaction(message=${originalMessage}, signatures=$signatures)"

  public fun serialize(): ByteArray {
    val signaturesSize = signatures.size
    val signaturesLength = ShortvecEncoding.encodeLength(signaturesSize)
    val bufferSize =
      signaturesLength.size +
          signaturesSize * TweetNaCl.Signature.SIGNATURE_BYTES +
          signedMessage.size
    val out = Buffer()
    out.write(signaturesLength)
    for (signature in signatures) {
      val rawSignature = signature.decodeBase58()
      out.write(rawSignature)
    }
    out.write(signedMessage)
    return out.readByteArray(bufferSize.toLong())
  }

  public fun validate(): Boolean {
    val message = signedMessage
    val messageLength = message.size
    val signaturesSize = signatures.size
    val signaturesLength = ShortvecEncoding.encodeLength(signaturesSize)
    val signaturesSizeBytes = signaturesLength.size
    val signatureSize = TweetNaCl.Signature.SIGNATURE_BYTES
    val signatureSizeBytes = ShortvecEncoding.encodeLength(signatureSize)
    val signatureSizeBytesLength = signatureSizeBytes.size
    val expectedSize =
      messageLength + signaturesSizeBytes + signaturesSize * (signatureSize + signatureSizeBytesLength)
    return expectedSize == serialize().size
  }
}
