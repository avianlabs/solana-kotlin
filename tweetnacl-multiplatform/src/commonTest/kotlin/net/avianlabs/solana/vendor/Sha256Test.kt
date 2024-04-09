package net.avianlabs.solana.tweetnacl.net.avianlabs.solana.vendor

import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import net.avianlabs.solana.tweetnacl.vendor.Sha256
import kotlin.test.Test
import kotlin.test.assertContentEquals

val string = "Hello, world!".encodeToByteArray()
val expected = "MV9b23bQeMQ7isAGTkoBZGErH853yGk0W/yUx1iU7dM=".decodeBase64Bytes()

class Sha256Test {

  @Test
  fun testParallelSha256() = runTest {
    launch(Dispatchers.Unconfined) {
      repeat(10000) {
        val actual = Sha256.digest(string)
        assertContentEquals(expected, actual)
      }
    }
    launch(Dispatchers.Unconfined) {
      repeat(10000) {
        val actual = Sha256.digest(string)
        assertContentEquals(expected, actual)
      }
    }
  }
}
