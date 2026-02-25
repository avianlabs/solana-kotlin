package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import net.avianlabs.solana.tweetnacl.vendor.encodeToBase58String
import net.avianlabs.solana.vendor.ShortVecEncoding
import okio.Buffer

@ConsistentCopyVisibility
public data class SignedTransaction internal constructor(
  public val originalMessage: Message,
  public val signedMessage: ByteArray,
  public val signatures: Map<PublicKey, ByteArray>,
) : Transaction(originalMessage) {

  public override fun sign(signers: List<Signer>): SignedTransaction = SignedTransaction(
    originalMessage = originalMessage,
    signedMessage = signedMessage,
    signatures = signatures + signers.associate { signer ->
      signer.publicKey to
        TweetNaCl.Signature.sign(signedMessage, signer.secretKey)
    }
  )

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
    "SignedTransaction(message=${originalMessage}, " +
      "signatures=${signatures.values.map { it.encodeToBase58String() }})"

  public fun serialize(includeNullSignatures: Boolean = false): SerializedTransaction {
    val signerKeys = message.accountKeys.filter { it.isSigner }
      .mapNotNull {
        signatures[it.publicKey]
          ?: ByteArray(TweetNaCl.Signature.SIGNATURE_BYTES).takeIf { includeNullSignatures }
      }
    val signaturesLength = ShortVecEncoding.encodeLength(signerKeys.size)
    val bufferSize =
      signaturesLength.size +
        signerKeys.size * TweetNaCl.Signature.SIGNATURE_BYTES +
        signedMessage.size
    val out = Buffer()
    out.write(signaturesLength)
    signerKeys.forEach(out::write)
    out.write(signedMessage)
    return SerializedTransaction(out.readByteArray(bufferSize.toLong()))
  }
}
