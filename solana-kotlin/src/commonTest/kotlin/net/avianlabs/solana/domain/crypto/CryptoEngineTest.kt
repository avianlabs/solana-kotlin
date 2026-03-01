package net.avianlabs.solana.domain.crypto

import net.avianlabs.solana.domain.core.Transaction
import net.avianlabs.solana.domain.program.SystemProgram
import net.avianlabs.solana.domain.randomKey
import net.avianlabs.solana.tweetnacl.vendor.encodeToBase58String
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

class CryptoEngineTest {
  @Test
  fun test_keyGeneration() {
    val keypair = randomKey()

    assertTrue(keypair.publicKey.isOnCurve(), "public key not in curve")
  }

  @Test
  fun test_sign() {
    val keypair = randomKey()
    val transaction = Transaction.Builder()
      .addInstruction(
        SystemProgram.createAccount(
          payer = keypair.publicKey,
          newAccount = randomKey().publicKey,
          lamports = 5000UL,
          space = 0UL,
          programAddress = SystemProgram.programId,
        )
      )
      .setRecentBlockHash(Random.nextBytes(32).encodeToBase58String())
      .build()
      .sign(keypair)

    val signature = transaction.signatures[keypair.publicKey]!!

    assertTrue(signature.size == 64, "wrong signature size ${signature.size}")
  }
}
