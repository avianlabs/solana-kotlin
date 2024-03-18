package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import net.avianlabs.solana.vendor.ShortvecEncoding
import net.avianlabs.solana.tweetnacl.vendor.decodeBase58
import net.avianlabs.solana.tweetnacl.vendor.encodeToBase58String
import okio.Buffer

public class Transaction(
  public val message: Message = Message(),
  private val _signatures: MutableList<String> = mutableListOf()
) {

  public val signatures: List<String>
    get() = _signatures

  private lateinit var serializedMessage: ByteArray

  public fun addInstruction(instruction: TransactionInstruction): Transaction {
    message.addInstruction(instruction)
    return this
  }

  public fun setRecentBlockHash(recentBlockhash: String): Transaction {
    message.recentBlockHash = recentBlockhash
    return this
  }

  public fun setFeePayer(feePayer: PublicKey?): Transaction {
    message.feePayer = feePayer
    return this
  }

  public fun sign(signer: Signer): Transaction = sign(listOf(signer))

  public fun sign(signers: List<Signer>): Transaction {
    require(signers.isNotEmpty()) { "No signers" }
    // Fee payer defaults to first signer if not set
    message.feePayer ?: let {
      message.feePayer = signers[0].publicKey
    }
    serializedMessage = message.serialize()
    for (signer in signers) {
      _signatures.add(
        TweetNaCl.Signature.sign(serializedMessage, signer.secretKey).encodeToBase58String()
      )
    }
    return this
  }

  public fun serialize(): ByteArray {
    val signaturesSize = signatures.size
    val signaturesLength = ShortvecEncoding.encodeLength(signaturesSize)
    val bufferSize =
      signaturesLength.size + signaturesSize * TweetNaCl.Signature.SIGNATURE_BYTES + serializedMessage.size
    val out = Buffer()
    out.write(signaturesLength)
    for (signature in signatures) {
      val rawSignature = signature.decodeBase58()
      out.write(rawSignature)
    }
    out.write(serializedMessage)
    return out.readByteArray(bufferSize.toLong())
  }

  override fun toString(): String {
    return """Transaction(
            |  signatures: [${signatures.joinToString()}],
            |  message: $message
        |)""".trimMargin()
  }

}
