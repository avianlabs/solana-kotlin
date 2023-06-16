package net.avianlabs.solana.domain.core

import net.avianlabs.solana.crypto.defaultCryptoEngine
import net.avianlabs.solana.vendor.ShortvecEncoding
import okio.Buffer
import org.komputing.kbase58.decodeBase58
import org.komputing.kbase58.encodeToBase58String

public class Transaction(
  public val message: Message = Message(),
  private val signatures: MutableList<String> = ArrayList(),
) {
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
      signatures.add(
        defaultCryptoEngine.sign(serializedMessage, signer.secretKey).encodeToBase58String()
      )
    }
    return this
  }

  public fun serialize(): ByteArray {
    val signaturesSize = signatures.size
    val signaturesLength = ShortvecEncoding.encodeLength(signaturesSize)
    val bufferSize =
      signaturesLength.size + signaturesSize * SIGNATURE_LENGTH + serializedMessage.size
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

private const val SIGNATURE_LENGTH = 64
