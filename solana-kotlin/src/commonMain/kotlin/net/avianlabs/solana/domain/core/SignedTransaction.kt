package net.avianlabs.solana.domain.core

import io.github.oshai.kotlinlogging.KotlinLogging
import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.vendor.decodeBase58
import net.avianlabs.solana.tweetnacl.vendor.encodeToBase58String
import net.avianlabs.solana.vendor.ShortVecEncoding
import okio.Buffer

private val logger = KotlinLogging.logger {}

@ConsistentCopyVisibility
public data class SignedTransaction internal constructor(
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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    if (!super.equals(other)) return false

    other as SignedTransaction

    if (originalMessage != other.originalMessage) return false
    if (!signedMessage.contentEquals(other.signedMessage)) return false
    if (signatures != other.signatures) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + originalMessage.hashCode()
    result = 31 * result + signedMessage.contentHashCode()
    result = 31 * result + signatures.hashCode()
    return result
  }

  override fun toString(): String =
    "SignedTransaction(message=${originalMessage}, signatures=$signatures)"

  public fun serialize(): SerializedTransaction {
    val signaturesSize = signatures.size
    val signaturesLength = ShortVecEncoding.encodeLength(signaturesSize)
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
}

public typealias SerializedTransaction = ByteArray
