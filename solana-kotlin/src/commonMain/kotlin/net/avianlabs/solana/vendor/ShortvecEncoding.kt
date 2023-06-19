package net.avianlabs.solana.vendor

import okio.Buffer
import kotlin.experimental.and

internal object ShortvecEncoding {
  internal fun encodeLength(len: Int): ByteArray {
    val buffer = Buffer()
    var length = len
    while (true) {
      val elem = length and 0x7f
      length = length ushr 7
      if (length == 0) {
        buffer.writeByte(elem)
        break
      } else {
        buffer.writeByte(elem or 0x80)
      }
    }
    return buffer.readByteArray()
  }

  internal fun decodeLength(bytes: ByteArray): Int {
    var len = 0
    var shift = 0
    for (i in bytes.indices) {
      val b = bytes[i]
      len = len or ((b and 0x7f).toInt() shl shift)
      if (b.toInt() and 0x80 == 0) {
        break
      }
      shift += 7
    }
    return len
  }
}
