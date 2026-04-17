@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package net.avianlabs.solana.tweetnacl

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.windows.BCRYPT_USE_SYSTEM_PREFERRED_RNG
import platform.windows.BCryptGenRandom

public actual fun secureRandomBytes(count: Int): ByteArray {
  val bytes = ByteArray(count)
  bytes.usePinned { pinned ->
    val status = BCryptGenRandom(
      null,
      pinned.addressOf(0).reinterpret(),
      count.toUInt(),
      BCRYPT_USE_SYSTEM_PREFERRED_RNG.toUInt(),
    )
    check(status == 0) { "BCryptGenRandom failed with status $status" }
  }
  return bytes
}
