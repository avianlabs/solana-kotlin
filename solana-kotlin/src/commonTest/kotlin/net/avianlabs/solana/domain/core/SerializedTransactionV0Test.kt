package net.avianlabs.solana.domain.core

import io.ktor.util.*
import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.ed25519.Ed25519Keypair
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import net.avianlabs.solana.vendor.ShortVecEncoding
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SerializedTransactionV0Test {

  private val keypair = Ed25519Keypair.fromBase58(
    "9bCpJHMBCpjCnJHCyEvwWqcnTPf4yxWWsNXU7AMYPyS4fsR1bSEPGfjHQ8TDfaQWofAHm8MVeSgQLeEia2uqvVy"
  )

  /**
   * Constructs a minimal V0 serialized transaction for testing.
   *
   * Wire format: [compact-u16 sig count][64-byte sigs...][v0 message bytes]
   * V0 message:  [0x80][3-byte header][compact-u16 account count][32-byte keys...]
   *              [32-byte blockhash][compact-u16 instruction count][instructions...]
   *              [compact-u16 ALT lookup count][ALT lookups...]
   */
  private fun buildMinimalV0Transaction(
    signerKey: PublicKey,
    numSignatures: Int = 1,
  ): SerializedTransaction {
    val recipientKey = PublicKey(ByteArray(32) { 0x42 })
    val programId = PublicKey(ByteArray(32) { 0x00 }) // SystemProgram
    val blockhash = ByteArray(32) { 0xAA.toByte() }

    // Build V0 message bytes
    val messageBuffer = Buffer().apply {
      // Version prefix (V0)
      writeByte(0x80)
      // Header: 1 required signature, 0 readonly signed, 1 readonly unsigned (program)
      writeByte(numSignatures) // numRequiredSignatures
      writeByte(0)             // numReadonlySignedAccounts
      writeByte(1)             // numReadonlyUnsignedAccounts
      // Account count: signer + recipient + program = 3
      write(ShortVecEncoding.encodeLength(3))
      // Account keys (order: signer first, then writable, then readonly)
      write(signerKey.toByteArray())
      write(recipientKey.toByteArray())
      write(programId.toByteArray())
      // Recent blockhash
      write(blockhash)
      // Instructions: 1 instruction
      write(ShortVecEncoding.encodeLength(1))
      // Instruction: programIdIndex=2, 2 account indices [0, 1], no data
      writeByte(2)
      write(ShortVecEncoding.encodeLength(2))
      writeByte(0)
      writeByte(1)
      write(ShortVecEncoding.encodeLength(0))
      // Address table lookups: none
      write(ShortVecEncoding.encodeLength(0))
    }
    val messageBytes = messageBuffer.readByteArray()

    // Build full transaction: 1 null signature + message
    val txBuffer = Buffer().apply {
      write(ShortVecEncoding.encodeLength(numSignatures))
      repeat(numSignatures) {
        write(ByteArray(TweetNaCl.Signature.SIGNATURE_BYTES))
      }
      write(messageBytes)
    }
    return SerializedTransaction(txBuffer.readByteArray())
  }

  @Test
  fun toSignedTransaction_v0_parsesCorrectly() {
    val tx = buildMinimalV0Transaction(keypair.publicKey)
    val signed = tx.toSignedTransaction()

    assertEquals(1, signed.signerKeys.size)
    assertEquals(keypair.publicKey, signed.signerKeys[0])
  }

  @Test
  fun toSignedTransaction_v0_signAndRoundTrip() {
    val tx = buildMinimalV0Transaction(keypair.publicKey)

    // Sign the V0 transaction
    val signed = tx.sign(keypair)

    // Parse it back
    val reParsed = signed.toSignedTransaction()

    assertEquals(1, reParsed.signerKeys.size)
    assertEquals(keypair.publicKey, reParsed.signerKeys[0])
    // Signature should be non-null (not all zeros)
    val sig = reParsed.signatures[keypair.publicKey]!!
    assertTrue(sig.any { it != 0.toByte() }, "Signature should not be all zeros after signing")
  }

  @Test
  fun toSignedTransaction_v0_serializeRoundTrip() {
    val tx = buildMinimalV0Transaction(keypair.publicKey)
    val originalBytes = tx.toByteArray()

    // Parse → re-serialize should produce same bytes
    val roundTripped = tx.toSignedTransaction().serialize(includeNullSignatures = true).toByteArray()

    assertContentEquals(originalBytes, roundTripped)
  }

  @Test
  fun toSignedTransaction_v0_resignProducesSameBytes() {
    val tx = buildMinimalV0Transaction(keypair.publicKey)

    val signed = tx.sign(keypair)
    val reSigned = signed.sign(keypair)

    assertContentEquals(signed.toByteArray(), reSigned.toByteArray())
  }

  @Test
  fun toSignedTransaction_v0_preservesVersionPrefix() {
    val tx = buildMinimalV0Transaction(keypair.publicKey)
    val signed = tx.toSignedTransaction()

    // The serialized message should start with 0x80 (V0 prefix)
    assertEquals(0x80.toByte(), signed.serializedMessage[0])
  }

  @Test
  fun toSignedTransaction_legacy_stillWorks() {
    // Verify existing legacy behavior is unaffected
    val signed = Transaction.Builder()
      .addInstruction(
        net.avianlabs.solana.domain.program.SystemProgram.transfer(
          keypair.publicKey,
          keypair.publicKey,
          1
        )
      )
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .build()
      .sign(keypair)

    val originalBytes = signed.serialize().toByteArray()
    val roundTripped = signed.serialize().toSignedTransaction().serialize().toByteArray()

    assertContentEquals(originalBytes, roundTripped)
  }

  @Test
  fun toSignedTransaction_unsupportedVersion_throws() {
    // Build a transaction with version 1 prefix (0x81)
    val messageBuffer = Buffer().apply {
      writeByte(0x81) // version 1
      writeByte(1)
      writeByte(0)
      writeByte(0)
      write(ShortVecEncoding.encodeLength(1))
      write(keypair.publicKey.toByteArray())
      write(ByteArray(32))
      write(ShortVecEncoding.encodeLength(0))
      write(ShortVecEncoding.encodeLength(0))
    }
    val messageBytes = messageBuffer.readByteArray()

    val txBuffer = Buffer().apply {
      write(ShortVecEncoding.encodeLength(1))
      write(ByteArray(TweetNaCl.Signature.SIGNATURE_BYTES))
      write(messageBytes)
    }
    val tx = SerializedTransaction(txBuffer.readByteArray())

    assertFailsWith<IllegalArgumentException> {
      tx.toSignedTransaction()
    }
  }

  @Test
  fun toSignedTransaction_v0_withAltLookups() {
    // Build a V0 transaction with address table lookups
    val altKey = PublicKey(ByteArray(32) { 0xBB.toByte() })
    val recipientKey = PublicKey(ByteArray(32) { 0x42 })
    val programId = PublicKey(ByteArray(32) { 0x00 })
    val blockhash = ByteArray(32) { 0xAA.toByte() }

    val messageBuffer = Buffer().apply {
      writeByte(0x80) // V0 prefix
      writeByte(1)    // numRequiredSignatures
      writeByte(0)    // numReadonlySignedAccounts
      writeByte(1)    // numReadonlyUnsignedAccounts
      // 3 static account keys
      write(ShortVecEncoding.encodeLength(3))
      write(keypair.publicKey.toByteArray())
      write(recipientKey.toByteArray())
      write(programId.toByteArray())
      write(blockhash)
      // 1 instruction
      write(ShortVecEncoding.encodeLength(1))
      writeByte(2)
      write(ShortVecEncoding.encodeLength(2))
      writeByte(0)
      writeByte(1)
      write(ShortVecEncoding.encodeLength(0))
      // 1 address table lookup
      write(ShortVecEncoding.encodeLength(1))
      write(altKey.toByteArray())
      // 1 writable index
      write(ShortVecEncoding.encodeLength(1))
      writeByte(3)
      // 0 readonly indices
      write(ShortVecEncoding.encodeLength(0))
    }
    val messageBytes = messageBuffer.readByteArray()

    val txBuffer = Buffer().apply {
      write(ShortVecEncoding.encodeLength(1))
      write(ByteArray(TweetNaCl.Signature.SIGNATURE_BYTES))
      write(messageBytes)
    }
    val tx = SerializedTransaction(txBuffer.readByteArray())

    // Should parse V0 with ALT lookups without error
    val signed = tx.toSignedTransaction()
    assertEquals(1, signed.signerKeys.size)
    assertEquals(keypair.publicKey, signed.signerKeys[0])

    // Round-trip preserves bytes
    val roundTripped = signed.serialize(includeNullSignatures = true).toByteArray()
    assertContentEquals(tx.toByteArray(), roundTripped)
  }
}
