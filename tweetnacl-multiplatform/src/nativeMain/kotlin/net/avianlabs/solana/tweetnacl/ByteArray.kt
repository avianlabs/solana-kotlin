@file:OptIn(ExperimentalForeignApi::class)

package net.avianlabs.solana.tweetnacl

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.get

internal fun CPointer<UByteVar>.toByteArray(length: Int): ByteArray {
  val nativeBytes = this
  val bytes = ByteArray(length)
  var index = 0
  while (index < length) {
    bytes[index] = nativeBytes[index].toByte()
    ++index
  }
  return bytes
}
