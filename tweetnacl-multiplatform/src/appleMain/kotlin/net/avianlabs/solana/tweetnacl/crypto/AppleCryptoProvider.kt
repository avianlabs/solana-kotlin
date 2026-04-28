@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package net.avianlabs.solana.tweetnacl.crypto

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault

internal class AppleCryptoProvider : NativeCryptoProviderBase() {
  override fun secureRandomBytes(count: Int): ByteArray {
    val bytes = ByteArray(count)
    bytes.usePinned { pinned ->
      val status = SecRandomCopyBytes(kSecRandomDefault, count.toULong(), pinned.addressOf(0))
      check(status == 0) { "SecRandomCopyBytes failed with status $status" }
    }
    return bytes
  }
}

internal actual fun platformCryptoProvider(): CryptoProvider = AppleCryptoProvider()
