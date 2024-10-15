package net.avianlabs.solana.tweetnacl

import net.avianlabs.solana.tweetnacl.ed25519.Ed25519Keypair
import net.avianlabs.solana.tweetnacl.vendor.decodeBase58
import kotlin.test.Test
import kotlin.test.assertEquals

class Ed25519Test {
  private val secretKey =
    "ftqmzZS6Va5xuyCdks47WZd1D8FpXZaPGqL3JE39814pReiEuTAvJpr8PxkUxm9wHKHTsfN8TGk44hhoEdQDYrD".decodeBase58()

  @Test
  fun test_chopping_and_restoring() {
    val chopped = secretKey.take(32).toByteArray()

    val restored = Ed25519Keypair.fromSecretKeyBytes(chopped).secretKey

    @OptIn(ExperimentalStdlibApi::class)
    assertEquals(
      secretKey.toHexString(),
      restored.toHexString(),
    )
  }
}
