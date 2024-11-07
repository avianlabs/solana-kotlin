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
        SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1)
      )
      .addInstruction(
        ComputeBudgetProgram.setComputeUnitPrice(1u)
      )
      .addInstruction(
        SystemProgram.transfer(keypair.publicKey, keypair.publicKey, 1)
      )
      .setRecentBlockHash("7qS6hDXGxd6ekYqnSqD7abG1jEfTcpfpjKApxWbb4gVF")
      .build()
      .sign(keypair)

    val serialized = transaction.serialize().encodeBase64()
    val expected =
      "AbY2fW8NzhzkDyTK3Av5Kn3/aBwxWWlGYjdMWHU4sLtT55yooXG3gKAFKCeQtYb7S86WOkWU6MVEsqP26vBw/gYBAAIDq" +
          "OvmfBiMqjpmh9Jg7DEAe1kg4Rnce0pv/ly9hIF7IyQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMGRm/l" +
          "IRcy/+ytunLDm+e8jOW7xfcSayxDmzpAAAAAZY5hBIuHu2Tv+5WayrPUoI8ytBhM3HsRYE3SA3zA6HoDAQIAAAwCAAA" +
          "AAQAAAAAAAAACAAkDAQAAAAAAAAABAgAADAIAAAABAAAAAAAAAA=="

    assertEquals(expected, serialized)
  }
}
