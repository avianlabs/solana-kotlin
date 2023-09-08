package net.avianlabs.solana.vendor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.avianlabs.solana.crypto.defaultCryptoEngine
import net.avianlabs.solana.crypto.isOnCurve
import net.avianlabs.solana.domain.core.PublicKey
import net.avianlabs.solana.domain.program.ProgramDerivedAddress
import net.avianlabs.solana.domain.program.associatedTokenAddress
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsOnCurveTest {
  @Test
  fun testIsOnCurve() {
    val offCurve = PublicKey.fromBase58("12rqwuEgBYiGhBrDJStCiqEtzQpTTiZbh7teNVLuYcFA")
    assertFalse(offCurve.isOnCurve())

    val onCurve = defaultCryptoEngine.generateKey().publicKey
    assertTrue(onCurve.isOnCurve())
  }

  @Test
  fun testParallelAssociatedAddress() {
    runBlocking {
      val x = PublicKey.fromBase58("4rZoSK72jVaAW1ayZLrefdMPAAStRVhCfH1PSundaoNt")
      val y = PublicKey.fromBase58("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v")
      val dx = ProgramDerivedAddress(PublicKey.fromBase58("BKN4phaq8HCe5bensWGCsgZoJ1JM3LGwbMP5FesbYY37"), 251u)
      launch {
        this.launch(Dispatchers.IO) {
          for (i in 0..100) {
            val result = x.associatedTokenAddress(y)
            assertEquals(dx, result)
          }
        }
        this.launch(Dispatchers.IO) {
          for (i in 0..100) {
            val result = x.associatedTokenAddress(y)
            assertEquals(dx, result)
          }
        }
      }
    }
  }
}
