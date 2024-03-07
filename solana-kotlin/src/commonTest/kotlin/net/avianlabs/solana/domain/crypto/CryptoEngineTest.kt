package net.avianlabs.solana.domain.crypto

import net.avianlabs.solana.crypto.isOnCurve
import net.avianlabs.solana.domain.core.TransactionBuilder
import net.avianlabs.solana.domain.program.SystemProgram
import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.vendor.decodeBase58
import net.avianlabs.solana.tweetnacl.vendor.encodeToBase58String
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

class CryptoEngineTest {
  @Test
  fun test_keyGeneration() {
    val keypair = TweetNaCl.Signature.generateKey(Random.nextBytes(32))

    assertTrue(keypair.publicKey.isOnCurve(), "public key not in curve")
  }

  @Test
  fun test_sign() {
    val keypair = TweetNaCl.Signature.generateKey(Random.nextBytes(32))
    val transaction = TransactionBuilder()
      .addInstruction(
        SystemProgram.createAccount(
          fromPublicKey = keypair.publicKey,
          newAccountPublicKey = TweetNaCl.Signature.generateKey(Random.nextBytes(32)).publicKey,
          lamports = 5000,
          space = 0,
        )
      )
      .setRecentBlockHash(Random.nextBytes(32).encodeToBase58String())
      .build()
      .sign(keypair)

    val signature = transaction.signatures.first()

    val signatureArray = signature.decodeBase58()

    assertTrue(signatureArray.size == 64, "wrong signature size ${signatureArray.size}")
  }
}