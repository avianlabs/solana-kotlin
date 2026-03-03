@file:Suppress("DEPRECATION")

package net.avianlabs.solana.domain.core

import io.ktor.util.*
import net.avianlabs.solana.domain.program.SystemProgram
import net.avianlabs.solana.tweetnacl.ed25519.Ed25519Keypair
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class SerializedTransactionTest {

  private val keypair = Ed25519Keypair.fromBase58(
    "9bCpJHMBCpjCnJHCyEvwWqcnTPf4yxWWsNXU7AMYPyS4fsR1bSEPGfjHQ8TDfaQWofAHm8MVeSgQLeEia2uqvVy"
  )

  private val keypair2 = Ed25519Keypair.fromBase58(
    "53iBnfgSVoZPEo9EtKnZ8yDSTyxNTxmqECrQs9nLJotjcsJVQCjTn6J7V8cgKe2umYGx9SpGdDocamV4tgkXP6Fr"
  )

  @Test
  fun roundTrip_signProducesSameBytes() {
    val signed = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair.publicKey)
      .build()
      .sign(keypair)

    val originalBytes = signed.serialize().toByteArray()

    // Re-signing the serialized transaction with the same key should produce the same bytes
    val reSigned = signed.serialize().sign(keypair).toByteArray()

    assertContentEquals(originalBytes, reSigned)
  }

  @Test
  fun signFromRawBytes() {
    val signed = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair.publicKey)
      .build()
      .sign(keypair)

    val expectedBase64 = signed.serialize().toByteArray().encodeBase64()

    // Serialize (includes null signature slots), then re-sign
    val fromRaw = signed.serialize()
      .sign(keypair)
      .toByteArray()
      .encodeBase64()

    assertEquals(expectedBase64, fromRaw)
  }

  @Test
  fun multiSigner_partialThenComplete() {
    // Build a 2-signer transaction, sign with only keypair first
    val partiallySigned = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair2.publicKey)
      .build()
      .sign(keypair)
      .serialize()

    // Now sign the serialized transaction with the missing keypair2
    val fullySigned = partiallySigned.sign(keypair2)

    // Build the reference: sign with both signers from scratch
    val reference = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair2.publicKey)
      .build()
      .sign(listOf(keypair, keypair2))
      .serialize()

    assertContentEquals(reference.toByteArray(), fullySigned.toByteArray())
  }

  @Test
  fun nullSignatureSlots_replacedCorrectly() {
    // Serialize with null signature slots (unsigned), then sign to fill them
    val withNullSlots = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair2.publicKey)
      .build()
      .serialize()

    // Sign with both signers
    val signed = withNullSlots.sign(listOf(keypair2, keypair))

    // Reference
    val reference = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair2.publicKey)
      .build()
      .sign(listOf(keypair, keypair2))
      .serialize()

    assertContentEquals(reference.toByteArray(), signed.toByteArray())
  }

  @Test
  fun toVersionedTransaction_roundTrip() {
    // Sign via builder, serialize, convert to VersionedTransaction, re-serialize
    val signed = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair.publicKey)
      .build()
      .sign(keypair)

    val originalBytes = signed.serialize().toByteArray()
    val roundTripped = signed.serialize().toVersionedTransaction().serialize().toByteArray()

    assertContentEquals(originalBytes, roundTripped)
  }

  @Test
  fun toVersionedTransaction_multiSigner_partialThenComplete() {
    // Build a 2-signer transaction, sign with only keypair first, serialize
    val partiallySerialized = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair2.publicKey)
      .build()
      .sign(keypair)
      .serialize()

    // Convert to VersionedTransaction, sign with missing signer, re-serialize
    val fullySigned = partiallySerialized
      .toVersionedTransaction()
      .sign(keypair2)
      .serialize()

    // Build the reference: sign with both signers from scratch
    val reference = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair2.publicKey)
      .build()
      .sign(listOf(keypair, keypair2))
      .serialize()

    assertContentEquals(reference.toByteArray(), fullySigned.toByteArray())
  }

  @Test
  fun equivalence_serializedSignMatchesBuilderSign() {
    // Sign via builder
    val viaBuilder = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair.publicKey)
      .build()
      .sign(keypair)
      .serialize()
      .toByteArray()

    // Sign via SerializedTransaction
    val viaSerialized = VersionedTransaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair.publicKey)
      .build()
      .serialize()
      .sign(keypair)
      .toByteArray()

    assertContentEquals(viaBuilder, viaSerialized)
  }
}
