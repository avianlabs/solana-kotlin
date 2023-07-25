package net.avianlabs.solana.vendor

import net.avianlabs.solana.crypto.defaultCryptoEngine
import net.avianlabs.solana.crypto.isOnCurve
import net.avianlabs.solana.domain.core.PublicKey
import org.junit.Test
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
}
