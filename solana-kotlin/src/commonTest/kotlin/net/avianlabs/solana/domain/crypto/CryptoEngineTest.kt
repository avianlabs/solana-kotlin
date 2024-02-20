package net.avianlabs.solana.domain.crypto

import net.avianlabs.solana.crypto.defaultCryptoEngine
import net.avianlabs.solana.crypto.defaultSecureRandom
import net.avianlabs.solana.crypto.isOnCurve
import net.avianlabs.solana.domain.core.TransactionBuilder
import net.avianlabs.solana.domain.program.SystemProgram
import net.avianlabs.solana.vendor.decodeBase58
import net.avianlabs.solana.vendor.encodeToBase58String
import kotlin.test.Test
import kotlin.test.assertTrue

class CryptoEngineTest {
  @Test
  fun test_keyGeneration() {
    val keypair = defaultCryptoEngine.generateKey()

    assertTrue(keypair.publicKey.isOnCurve(), "public key not in curve")
  }

  @Test
  fun test_sign() {
    val keypair = defaultCryptoEngine.generateKey()
    val transaction = TransactionBuilder()
      .addInstruction(
        SystemProgram.createAccount(
          fromPublicKey = keypair.publicKey,
          newAccountPublicKey = defaultCryptoEngine.generateKey().publicKey,
          lamports = 5000,
          space = 0,
        )
      )
      .setRecentBlockHash(defaultSecureRandom.randomBytes(32).encodeToBase58String())
      .build()
      .sign(keypair)

    val signature = transaction.signatures.first()

    val signatureArray = signature.decodeBase58()
    
    assertTrue(signatureArray.size == 64, "wrong signature size ${signatureArray.size}")
  }
}