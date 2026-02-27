package net.avianlabs.solana.domain.core

import io.ktor.util.*
import net.avianlabs.solana.domain.program.SystemProgram
import net.avianlabs.solana.tweetnacl.ed25519.Ed25519Keypair
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class VersionedTransactionTest {

  private val keypair = Ed25519Keypair.fromBase58(
    "9bCpJHMBCpjCnJHCyEvwWqcnTPf4yxWWsNXU7AMYPyS4fsR1bSEPGfjHQ8TDfaQWofAHm8MVeSgQLeEia2uqvVy"
  )

  private val keypair2 = Ed25519Keypair.fromBase58(
    "53iBnfgSVoZPEo9EtKnZ8yDSTyxNTxmqECrQs9nLJotjcsJVQCjTn6J7V8cgKe2umYGx9SpGdDocamV4tgkXP6Fr"
  )

  @Test
  fun noALTs_producesLegacyMessage() {
    val vtx = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair.publicKey)
      .build()

    assertIs<VersionedMessage.Legacy>(vtx.message)
  }

  @Test
  fun noALTs_matchesLegacyTransaction() {
    // VersionedTransaction without ALTs should produce identical bytes to legacy Transaction
    val legacySigned = Transaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .build()
      .sign(keypair)

    val versionedSigned = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .build()
      .sign(keypair)

    assertContentEquals(
      legacySigned.serialize().toByteArray(),
      versionedSigned.serialize().toByteArray(),
    )
  }

  @Test
  fun withALT_producesV0Message() {
    val recipientKey = PublicKey(ByteArray(32) { 0x42 })
    val altAddress1 = PublicKey(ByteArray(32) { 0xAA.toByte() })
    val altKey = PublicKey(ByteArray(32) { 0xBB.toByte() })

    val alt = AddressLookupTableAccount(
      key = altKey,
      addresses = listOf(altAddress1, recipientKey),
    )

    val vtx = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, recipientKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair.publicKey)
      .addAddressLookupTableAccount(alt)
      .build()

    val message = assertIs<VersionedMessage.V0>(vtx.message)

    // recipientKey should be in ALT lookup, not static keys
    // Static: keypair (signer) + SystemProgram (program)
    assertEquals(2, message.staticAccountKeys.size)
    assertEquals(1, message.addressTableLookups.size)
    assertEquals(altKey, message.addressTableLookups[0].accountKey)
    // recipientKey is writable in the transfer instruction
    assertEquals(1, message.addressTableLookups[0].writableIndexes.size)
    assertEquals(1.toUByte(), message.addressTableLookups[0].writableIndexes[0])
  }

  @Test
  fun withALT_signAndRoundTrip() {
    val recipientKey = PublicKey(ByteArray(32) { 0x42 })
    val altKey = PublicKey(ByteArray(32) { 0xBB.toByte() })

    val alt = AddressLookupTableAccount(
      key = altKey,
      addresses = listOf(recipientKey),
    )

    val signed = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, recipientKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair.publicKey)
      .addAddressLookupTableAccount(alt)
      .build()
      .sign(keypair)

    // Serialized message should start with 0x80 (V0 prefix)
    assertEquals(0x80.toByte(), signed.serializedMessage[0])

    // Sign → serialize → parse → verify signer
    val serialized = signed.serialize()
    val reParsed = serialized.toSignedTransaction()
    assertEquals(1, reParsed.signerKeys.size)
    assertEquals(keypair.publicKey, reParsed.signerKeys[0])
  }

  @Test
  fun signersStayStatic() {
    // Even if a signer appears in an ALT, it must remain in static keys
    val altKey = PublicKey(ByteArray(32) { 0xBB.toByte() })
    val recipientKey = PublicKey(ByteArray(32) { 0x42 })

    val alt = AddressLookupTableAccount(
      key = altKey,
      addresses = listOf(keypair.publicKey, recipientKey),
    )

    val vtx = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, recipientKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair.publicKey)
      .addAddressLookupTableAccount(alt)
      .build()

    val message = assertIs<VersionedMessage.V0>(vtx.message)
    // Signer should always be in static keys
    assertTrue(message.staticAccountKeys.any { it.publicKey == keypair.publicKey })
  }

  @Test
  fun programIdsStayStatic() {
    // Program IDs must always be in static keys, never in ALT lookups
    val recipientKey = PublicKey(ByteArray(32) { 0x42 })
    val altKey = PublicKey(ByteArray(32) { 0xBB.toByte() })

    val alt = AddressLookupTableAccount(
      key = altKey,
      addresses = listOf(recipientKey, SystemProgram.programId),
    )

    val vtx = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, recipientKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair.publicKey)
      .addAddressLookupTableAccount(alt)
      .build()

    val message = assertIs<VersionedMessage.V0>(vtx.message)
    // SystemProgram should be static
    assertTrue(message.staticAccountKeys.any { it.publicKey == SystemProgram.programId })
  }

  @Test
  fun feePayerAutoSet_fromFirstSigner() {
    val vtx = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .build()

    // sign() should set fee payer from first signer
    val signed = vtx.sign(keypair)
    assertEquals(1, signed.signerKeys.size)
    assertEquals(keypair.publicKey, signed.signerKeys[0])
  }
}
