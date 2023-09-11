package net.avianlabs.solana.vendor

import io.ktor.util.decodeBase64Bytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

val string = "Hello, world!".encodeToByteArray()
val expected = "MV9b23bQeMQ7isAGTkoBZGErH853yGk0W/yUx1iU7dM=".decodeBase64Bytes()

class Sha256Test {

  @Test
  fun testParallelSha256() {
    runBlocking {
      launch(Dispatchers.Unconfined) {
        repeat(10000) {
          val actual = Sha256.digest(string)
          assert(expected.contentEquals(actual))
        }
      }
      launch(Dispatchers.Unconfined) {
        repeat(10000) {
          val actual = Sha256.digest(string)
          assert(expected.contentEquals(actual))
        }
      }
    }
  }
}
