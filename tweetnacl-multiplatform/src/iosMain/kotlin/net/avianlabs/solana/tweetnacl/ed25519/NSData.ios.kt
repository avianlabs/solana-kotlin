@file:OptIn(
  ExperimentalForeignApi::class,
  BetaInteropApi::class,
)

package net.avianlabs.solana.tweetnacl.ed25519

import kotlinx.cinterop.*
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

public fun NSData.toKotlinByteArray(): ByteArray = ByteArray(length.toInt()).apply {
  usePinned { pinned: Pinned<ByteArray> ->
    memcpy(
      __dst = pinned.addressOf(0),
      __src = this@toKotlinByteArray.bytes,
      __n = this@toKotlinByteArray.length,
    )
  }
}

public fun ByteArray.toNSData(): NSData = memScoped {
  NSData.create(
    bytes = allocArrayOf(this@toNSData),
    length = size.toULong(),
  )
}