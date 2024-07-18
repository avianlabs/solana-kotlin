package net.avianlabs.solana.tweetnacl

import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalStdlibApi::class)
class SecretBoxTest {
  private val secretKey =
    "afa0cf62032d72ceb94224a7c658d01081810cc83034306e7b0bb3dd36bf9f4c".hexToByteArray()

  @Test
  fun test_encryption() {
    val result = TweetNaCl.SecretBox(secretKey)
      .box("Hello world!".encodeToByteArray(), ByteArray(24))

    assertEquals(
      "6b6608e77e7c4024147baa01d7576a3a5a2852a6278bac8eca4db1e6",
      result!!.toHexString(),
    )
  }

  @Test
  fun test_decryption() {
    val result = TweetNaCl.SecretBox(secretKey)
      .open(
        "6b6608e77e7c4024147baa01d7576a3a5a2852a6278bac8eca4db1e6".hexToByteArray(),
        ByteArray(24)
      )

    assertEquals(
      "Hello world!",
      result!!.decodeToString(),
    )
  }
}
