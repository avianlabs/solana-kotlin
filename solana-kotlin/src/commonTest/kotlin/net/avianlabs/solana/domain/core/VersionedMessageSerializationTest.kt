package net.avianlabs.solana.domain.core

import net.avianlabs.solana.domain.program.SystemProgram
import net.avianlabs.solana.tweetnacl.ed25519.Ed25519Keypair
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import kotlin.test.Test
import kotlin.test.assertEquals

class VersionedMessageSerializationTest {

  private val keypair = Ed25519Keypair.fromBase58(
    "9bCpJHMBCpjCnJHCyEvwWqcnTPf4yxWWsNXU7AMYPyS4fsR1bSEPGfjHQ8TDfaQWofAHm8MVeSgQLeEia2uqvVy"
  )

  @Test
  fun v0_serialize_startsWithVersionPrefix() {
    val recipientKey = PublicKey(ByteArray(32) { 0x42 })
    val altKey = PublicKey(ByteArray(32) { 0xBB.toByte() })

    val alt = AddressLookupTableAccount(
      key = altKey,
      addresses = listOf(recipientKey),
    )

    val vtx = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, recipientKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair.publicKey)
      .addAddressLookupTableAccount(alt)
      .build()

    val serialized = vtx.message.serialize()
    assertEquals(0x80.toByte(), serialized[0], "V0 message should start with 0x80")
  }

  @Test
  fun legacy_serialize_matchesMessageSerialize() {
    val message = Message.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair.publicKey)
      .build()

    val directSerialized = message.serialize()
    val wrappedSerialized = VersionedMessage.Legacy(message).serialize()

    assertEquals(directSerialized.toList(), wrappedSerialized.toList())
  }

  @Test
  fun v0_header_computedFromStaticKeysOnly() {
    val recipientKey = PublicKey(ByteArray(32) { 0x42 })
    val altKey = PublicKey(ByteArray(32) { 0xBB.toByte() })

    val alt = AddressLookupTableAccount(
      key = altKey,
      addresses = listOf(recipientKey),
    )

    val vtx = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, recipientKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair.publicKey)
      .addAddressLookupTableAccount(alt)
      .build()

    val serialized = vtx.message.serialize()
    // Byte 1 = numRequiredSignatures (after 0x80 prefix)
    // Only the signer (keypair) is a required signer
    assertEquals(1, serialized[1].toInt(), "numRequiredSignatures should count static signers only")
  }
}
