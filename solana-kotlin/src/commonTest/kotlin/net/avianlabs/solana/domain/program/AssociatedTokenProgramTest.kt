package net.avianlabs.solana.domain.program

import net.avianlabs.solana.domain.randomKey
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import kotlin.test.Test
import kotlin.test.assertEquals

class AssociatedTokenProgramTest {

  @Test
  fun testFindProgramAddress() {
    for (i in 0..1000) {
      val programId = randomKey().publicKey

      val seeds = listOf(
        "Lil'".encodeToByteArray(),
        "Bits".encodeToByteArray(),
      )
      val address = Program.findProgramAddress(
        seeds = seeds,
        programId = programId,
      )

      val created = Program.createProgramAddress(
        seeds = seeds + byteArrayOf(address.nonce.toByte()),
        programId = programId,
      )

      assertEquals(address.address, created)
    }
  }

  @Test
  fun testKnownAddress() {
    val knownGoodPairs = listOf(
      "2g16Nrc8oHn8nL22qS9CVZVkH1Z7oVPZWYpWdxZCJueq" to "8tmU56dB6xhxjcTuMxkgQxPhbfFGyuMc3wXq4TBQ6rDK",
      "83uzQeZicEzGqb4JCB2pSX3pc5y4fKayb941kVzuNkpb" to "AaQtastXFgNAYCnikKtomeCgN12bxnPs5hA2sxVceXFR",
    )
    val mint = PublicKey.fromBase58("4zMMC9srt5Ri5X14GAgXhaHii3GnPAEERYPJgZJDncDU")

    for ((wallet, expectedAssociated) in knownGoodPairs) {
      val wallet = PublicKey.fromBase58(wallet)

      val associated = wallet.associatedTokenAddress(mint)

      val expectedAssociated = PublicKey.fromBase58(expectedAssociated)

      assertEquals(expectedAssociated, associated.address)
    }
  }
}
