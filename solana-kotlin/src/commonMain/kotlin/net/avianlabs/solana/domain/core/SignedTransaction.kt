package net.avianlabs.solana.domain.core

import co.touchlab.skie.configuration.annotations.SkieVisibility
import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import net.avianlabs.solana.tweetnacl.vendor.encodeToBase58String
import net.avianlabs.solana.vendor.ShortVecEncoding

@Deprecated(
  message = "Use VersionedTransaction instead",
  replaceWith = ReplaceWith("VersionedTransaction"),
)
public data class SignedTransaction(
  public val message: VersionedMessage,
  public val serializedMessage: ByteArray,
  public val signatures: Map<PublicKey, ByteArray>,
  public val signerKeys: List<PublicKey>,
) {

  public companion object {
    public fun sign(message: VersionedMessage, signers: List<Signer>): SignedTransaction {
      val serializedMessage = message.serialize()
      val signerKeys = message.staticAccountKeys
        .filter { it.isSigner }
        .map { it.publicKey }
      val signatures = signers.associate { signer ->
        signer.publicKey to
          TweetNaCl.Signature.sign(serializedMessage, signer.secretKey)
      }
      return SignedTransaction(
        message = message,
        serializedMessage = serializedMessage,
        signatures = signatures,
        signerKeys = signerKeys,
      )
    }
  }

  public fun sign(signer: Signer): SignedTransaction = sign(listOf(signer))

  public fun sign(signers: List<Signer>): SignedTransaction = SignedTransaction(
    message = message,
    serializedMessage = serializedMessage,
    signerKeys = signerKeys,
    signatures = signatures + signers.associate { signer ->
      signer.publicKey to
        TweetNaCl.Signature.sign(serializedMessage, signer.secretKey)
    },
  )

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as SignedTransaction

    if (!serializedMessage.contentEquals(other.serializedMessage)) return false
    if (signatures != other.signatures) return false
    if (signerKeys != other.signerKeys) return false

    return true
  }

  override fun hashCode(): Int {
    var result = serializedMessage.contentHashCode()
    result = 31 * result + signatures.hashCode()
    result = 31 * result + signerKeys.hashCode()
    return result
  }

  override fun toString(): String =
    "SignedTransaction(signatures=${signatures.values.map { it.encodeToBase58String() }})"

  @SkieVisibility.PublicButReplaced
  public fun serialize(includeNullSignatures: Boolean = false): SerializedTransaction {
    val orderedSigs = signerKeys.mapNotNull {
      signatures[it]
        ?: ByteArray(TweetNaCl.Signature.SIGNATURE_BYTES).takeIf { includeNullSignatures }
    }
    val signaturesLength = ShortVecEncoding.encodeLength(orderedSigs.size)
    val bufferSize =
      signaturesLength.size +
        orderedSigs.size * TweetNaCl.Signature.SIGNATURE_BYTES +
        serializedMessage.size
    val out = Buffer()
    out.write(signaturesLength)
    orderedSigs.forEach(out::write)
    out.write(serializedMessage)
    return SerializedTransaction(out.readByteArray(bufferSize))
  }
}
