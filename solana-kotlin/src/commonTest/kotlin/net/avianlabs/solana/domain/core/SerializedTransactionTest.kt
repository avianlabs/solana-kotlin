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
    val signed = Transaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .build()
      .sign(keypair)

    val originalBytes = signed.serialize().toByteArray()

    // Re-signing the serialized transaction with the same key should produce the same bytes
    val reSigned = signed.serialize().sign(keypair).toByteArray()

    assertContentEquals(originalBytes, reSigned)
  }

  @Test
  fun signFromRawBytes() {
    val signed = Transaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .build()
      .sign(keypair)

    val expectedBase64 = signed.serialize().toByteArray().encodeBase64()

    // Construct a SerializedTransaction from raw bytes and sign it
    val fromRaw = signed.serialize(includeNullSignatures = true)
      .sign(keypair)
      .toByteArray()
      .encodeBase64()

    assertEquals(expectedBase64, fromRaw)
  }

  @Test
  fun multiSigner_partialThenComplete() {
    // Build a 2-signer transaction, sign with only keypair first
    val partiallySigned = Transaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair2.publicKey)
      .build()
      .sign(keypair)
      .serialize(includeNullSignatures = true)

    // Now sign the serialized transaction with the missing keypair2
    val fullySigned = partiallySigned.sign(keypair2)

    // Build the reference: sign with both signers from scratch
    val reference = Transaction.Builder()
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
    // Serialize with null signature slots, then sign to fill them
    val withNullSlots = Transaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair2.publicKey)
      .build()
      .sign(emptyList())
      .serialize(includeNullSignatures = true)

    // Sign with both signers
    val signed = withNullSlots.sign(listOf(keypair2, keypair))

    // Reference
    val reference = Transaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair2.publicKey)
      .build()
      .sign(listOf(keypair, keypair2))
      .serialize()

    assertContentEquals(reference.toByteArray(), signed.toByteArray())
  }

  @Test
  fun equivalence_serializedSignMatchesBuilderSign() {
    // Sign via builder
    val viaBuilder = Transaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .build()
      .sign(keypair)
      .serialize()
      .toByteArray()

    // Sign via SerializedTransaction
    val viaSerialized = Transaction.Builder()
      .addInstruction(SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1))
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair.publicKey)
      .build()
      .sign(emptyList())
      .serialize(includeNullSignatures = true)
      .sign(keypair)
      .toByteArray()

    assertContentEquals(viaBuilder, viaSerialized)
  }
}
