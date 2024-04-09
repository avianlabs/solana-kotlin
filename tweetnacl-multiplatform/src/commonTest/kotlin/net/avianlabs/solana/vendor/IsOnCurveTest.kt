package net.avianlabs.solana.tweetnacl.net.avianlabs.solana.vendor

import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsOnCurveTest {
  @Test
  fun testIsOnCurve() {
    val offCurve = PublicKey.fromBase58("12rqwuEgBYiGhBrDJStCiqEtzQpTTiZbh7teNVLuYcFA")
    assertFalse(offCurve.isOnCurve())

    val onCurve = TweetNaCl.Signature.generateKey(Random.nextBytes(32)).publicKey
    assertTrue(onCurve.isOnCurve())
  }
}
