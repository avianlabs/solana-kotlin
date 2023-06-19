package net.avianlabs.solana.vendor

import org.junit.Test
import org.komputing.kbase58.encodeToBase58String

class Testing {
  @Test
  fun test() {

    println(TweetNaclFast.Signature.keyPair().secretKey.encodeToBase58String())
  }

}
