package net.avianlabs.solana.domain.core

import io.ktor.util.*
import net.avianlabs.solana.domain.program.ComputeBudgetProgram
import net.avianlabs.solana.domain.program.SystemProgram
import net.avianlabs.solana.tweetnacl.ed25519.Ed25519Keypair
import kotlin.test.Test
import kotlin.test.assertEquals

class TransactionTest {
  @Test
  fun test_serialization() {
    val keypair = Ed25519Keypair.fromBase58(
      "9bCpJHMBCpjCnJHCyEvwWqcnTPf4yxWWsNXU7AMYPyS4fsR1bSEPGfjHQ8TDfaQWofAHm8MVeSgQLeEia2uqvVy"
    )

    val transaction = Transaction.Builder()
      .addInstruction(
        SystemProgram.transferSol(keypair.publicKey, keypair.publicKey, 1UL)
      )
      .addInstruction(
        ComputeBudgetProgram.setComputeUnitPrice(1UL)
      )
      .addInstruction(
        SystemProgram.transferSol(keypair.publicKey, keypair.publicKey, 1UL)
      )
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .build()
      .sign(keypair)

    val serialized = transaction.serialize().toByteArray().encodeBase64()
    val expected =
      "AbY2fW8NzhzkDyTK3Av5Kn3/aBwxWWlGYjdMWHU4sLtT55yooXG3gKAFKCeQtYb7S86WOkWU6MVEsqP26vBw/gYBAA" +
          "IDqOvmfBiMqjpmh9Jg7DEAe1kg4Rnce0pv/ly9hIF7IyQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
          "MGRm/lIRcy/+ytunLDm+e8jOW7xfcSayxDmzpAAAAAZY5hBIuHu2Tv+5WayrPUoI8ytBhM3HsRYE3SA3zA6HoDAQ" +
          "IAAAwCAAAAAQAAAAAAAAACAAkDAQAAAAAAAAABAgAADAIAAAABAAAAAAAAAA=="

    assertEquals(expected, serialized)
  }

  @Test
  fun test_sign() {
    val keypair = Ed25519Keypair.fromBase58(
      "9bCpJHMBCpjCnJHCyEvwWqcnTPf4yxWWsNXU7AMYPyS4fsR1bSEPGfjHQ8TDfaQWofAHm8MVeSgQLeEia2uqvVy"
    )

    val keypair2 = Ed25519Keypair.fromBase58(
      "53iBnfgSVoZPEo9EtKnZ8yDSTyxNTxmqECrQs9nLJotjcsJVQCjTn6J7V8cgKe2umYGx9SpGdDocamV4tgkXP6Fr"
    )

    val transaction = Transaction.Builder()
      .addInstruction(
        SystemProgram.transferSol(keypair.publicKey, keypair.publicKey, 1UL)
      )
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair2.publicKey)
      .build()
      .sign(keypair)
      .serialize(includeNullSignatures = false)

    val expected =
      "AXWGwDjk7s+ybacDFIIwXfVqO+Wuo17TlD9hGg76MrWihSWz6mUF3mMoengeRLsKS6LS9GfUArTK9tLsBeB2YggCAA" +
          "EDbtBATs2PPthWHf3bqIU5/bs3SYSvA9m1WaJcIXO3XIGo6+Z8GIyqOmaH0mDsMQB7WSDhGdx7Sm/+XL2EgXsjJA" +
          "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAZY5hBIuHu2Tv+5WayrPUoI8ytBhM3HsRYE3SA3zA6HoBAg" +
          "IBAQwCAAAAAQAAAAAAAAA="

    assertEquals(expected, transaction.toByteArray().encodeBase64())
  }

  @Test
  fun test_sign_null_signatures() {
    val keypair = Ed25519Keypair.fromBase58(
      "9bCpJHMBCpjCnJHCyEvwWqcnTPf4yxWWsNXU7AMYPyS4fsR1bSEPGfjHQ8TDfaQWofAHm8MVeSgQLeEia2uqvVy"
    )

    val keypair2 = Ed25519Keypair.fromBase58(
      "53iBnfgSVoZPEo9EtKnZ8yDSTyxNTxmqECrQs9nLJotjcsJVQCjTn6J7V8cgKe2umYGx9SpGdDocamV4tgkXP6Fr"
    )

    val transaction = Transaction.Builder()
      .addInstruction(
        SystemProgram.transferSol(keypair.publicKey, keypair.publicKey, 1UL)
      )
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .setFeePayer(keypair2.publicKey)
      .build()
      .sign(keypair)
      .serialize(includeNullSignatures = true)

    val expected =
      "AgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB1hs" +
          "A45O7Psm2nAxSCMF31ajvlrqNe05Q/YRoO+jK1ooUls+plBd5jKHp4HkS7Ckui0vRn1AK0yvbS7AXgdmIIAgABA2" +
          "7QQE7Njz7YVh3926iFOf27N0mErwPZtVmiXCFzt1yBqOvmfBiMqjpmh9Jg7DEAe1kg4Rnce0pv/ly9hIF7IyQAAA" +
          "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAGWOYQSLh7tk7/uVmsqz1KCPMrQYTNx7EWBN0gN8wOh6AQICAQ" +
          "EMAgAAAAEAAAAAAAAA"

    assertEquals(expected, transaction.toByteArray().encodeBase64())
  }
}
