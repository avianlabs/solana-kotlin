package net.avianlabs.solana.domain.core

import net.avianlabs.solana.domain.program.SystemProgram
import net.avianlabs.solana.tweetnacl.ed25519.Ed25519Keypair
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import net.avianlabs.solana.vendor.ShortVecEncoding
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeserializeVersionedMessageTest {

  private val keypair = Ed25519Keypair.fromBase58(
    "9bCpJHMBCpjCnJHCyEvwWqcnTPf4yxWWsNXU7AMYPyS4fsR1bSEPGfjHQ8TDfaQWofAHm8MVeSgQLeEia2uqvVy"
  )

  @Test
  fun legacy_roundTrip_bytesMatch() {
    val message = Message.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair.publicKey)
      .build()

    val serialized = message.serialize()
    val deserialized = VersionedMessage.deserialize(serialized)
    val reSerialized = deserialized.serialize()

    assertContentEquals(serialized, reSerialized)
  }

  @Test
  fun v0_roundTrip_bytesMatch() {
    // V0 message without ALT lookups: all instructions reference only static keys,
    // so deserialize → re-serialize produces identical bytes.
    val recipientKey = PublicKey(ByteArray(32) { 0x42 })

    val messageBuffer = Buffer().apply {
      writeByte(0x80) // V0 prefix
      writeByte(1)    // numRequiredSignatures
      writeByte(0)    // numReadonlySignedAccounts
      writeByte(1)    // numReadonlyUnsignedAccounts (program)
      write(ShortVecEncoding.encodeLength(3))
      write(keypair.publicKey.toByteArray())
      write(recipientKey.toByteArray())
      write(PublicKey(ByteArray(32) { 0x00 }).toByteArray()) // SystemProgram
      write(ByteArray(32) { 0xAA.toByte() }) // blockhash
      // 1 instruction referencing static keys only
      write(ShortVecEncoding.encodeLength(1))
      writeByte(2) // programIdIndex
      write(ShortVecEncoding.encodeLength(2))
      writeByte(0) // account index 0
      writeByte(1) // account index 1
      write(ShortVecEncoding.encodeLength(0)) // no data
      // 0 ALT lookups
      write(ShortVecEncoding.encodeLength(0))
    }
    val serialized = messageBuffer.readByteArray()

    val deserialized = VersionedMessage.deserialize(serialized)
    val reSerialized = deserialized.serialize()

    assertContentEquals(serialized, reSerialized)
  }

  @Test
  fun legacy_accountMetaFlags_reconstructedCorrectly() {
    val recipient = PublicKey(ByteArray(32) { 0x42 })
    val message = Message.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, recipient, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair.publicKey)
      .build()

    val serialized = message.serialize()
    val deserialized = VersionedMessage.deserialize(serialized)

    val accounts = deserialized.staticAccountKeys
    // Fee payer: signer + writable
    val feePayer = accounts.first { it.publicKey == keypair.publicKey }
    assertEquals(true, feePayer.isSigner)
    assertEquals(true, feePayer.isWritable)

    // Recipient: not a signer, writable
    val recipientMeta = accounts.first { it.publicKey == recipient }
    assertEquals(false, recipientMeta.isSigner)
    assertEquals(true, recipientMeta.isWritable)

    // SystemProgram: not a signer, not writable
    val programMeta = accounts.first { it.publicKey == SystemProgram.programId }
    assertEquals(false, programMeta.isSigner)
    assertEquals(false, programMeta.isWritable)
  }

  @Test
  fun legacy_instructionDecompilation_matchesOriginal() {
    val recipient = PublicKey(ByteArray(32) { 0x42 })
    val originalInstruction = SystemProgram.transfer(keypair.publicKey, recipient, 1)

    val message = Message.Builder()
      .addInstruction(originalInstruction)
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair.publicKey)
      .build()

    val serialized = message.serialize()
    val deserialized = VersionedMessage.deserialize(serialized)

    assertEquals(1, deserialized.instructions.size)
    val instruction = deserialized.instructions[0]
    assertEquals(originalInstruction.programId, instruction.programId)
    assertContentEquals(originalInstruction.data, instruction.data)
    assertEquals(originalInstruction.keys.size, instruction.keys.size)
    for (i in originalInstruction.keys.indices) {
      assertEquals(originalInstruction.keys[i].publicKey, instruction.keys[i].publicKey)
    }
  }

  @Test
  fun v0_altLookups_deserializedCorrectly() {
    val altKey = PublicKey(ByteArray(32) { 0xBB.toByte() })
    val recipientKey = PublicKey(ByteArray(32) { 0x42 })

    val messageBuffer = Buffer().apply {
      writeByte(0x80) // V0 prefix
      writeByte(1)    // numRequiredSignatures
      writeByte(0)    // numReadonlySignedAccounts
      writeByte(1)    // numReadonlyUnsignedAccounts (program)
      write(ShortVecEncoding.encodeLength(3))
      write(keypair.publicKey.toByteArray())
      write(recipientKey.toByteArray())
      write(PublicKey(ByteArray(32) { 0x00 }).toByteArray()) // SystemProgram
      write(ByteArray(32) { 0xAA.toByte() }) // blockhash
      // 1 instruction
      write(ShortVecEncoding.encodeLength(1))
      writeByte(2)
      write(ShortVecEncoding.encodeLength(2))
      writeByte(0)
      writeByte(1)
      write(ShortVecEncoding.encodeLength(0))
      // 1 ALT lookup
      write(ShortVecEncoding.encodeLength(1))
      write(altKey.toByteArray())
      write(ShortVecEncoding.encodeLength(2))
      writeByte(3)
      writeByte(5)
      write(ShortVecEncoding.encodeLength(1))
      writeByte(7)
    }
    val bytes = messageBuffer.readByteArray()

    val deserialized = VersionedMessage.deserialize(bytes)
    val v0 = deserialized as VersionedMessage.V0

    assertEquals(1, v0.addressTableLookups.size)
    val lookup = v0.addressTableLookups[0]
    assertEquals(altKey, lookup.accountKey)
    assertEquals(listOf(3.toUByte(), 5.toUByte()), lookup.writableIndexes)
    assertEquals(listOf(7.toUByte()), lookup.readonlyIndexes)
  }

  @Test
  fun unsupportedVersion_throws() {
    val messageBuffer = Buffer().apply {
      writeByte(0x81) // version 1
      writeByte(1)
      writeByte(0)
      writeByte(0)
      write(ShortVecEncoding.encodeLength(1))
      write(keypair.publicKey.toByteArray())
      write(ByteArray(32))
      write(ShortVecEncoding.encodeLength(0))
    }
    val bytes = messageBuffer.readByteArray()

    assertFailsWith<IllegalArgumentException> {
      VersionedMessage.deserialize(bytes)
    }
  }
}
