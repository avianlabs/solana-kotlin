@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package net.avianlabs.solana.tweetnacl

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.posix.O_RDONLY
import platform.posix.close
import platform.posix.open
import platform.posix.read

public actual fun secureRandomBytes(count: Int): ByteArray {
  val bytes = ByteArray(count)
  val fd = open("/dev/urandom", O_RDONLY)
  check(fd >= 0) { "Failed to open /dev/urandom" }
  try {
    var offset = 0
    bytes.usePinned { pinned ->
      while (offset < count) {
        val bytesRead = read(fd, pinned.addressOf(offset), (count - offset).convert())
        check(bytesRead > 0) { "Failed to read from /dev/urandom" }
        offset += bytesRead.toInt()
      }
    }
  } finally {
    close(fd)
  }
  return bytes
}
