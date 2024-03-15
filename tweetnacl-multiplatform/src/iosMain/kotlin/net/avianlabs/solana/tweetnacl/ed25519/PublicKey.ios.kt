@file:OptIn(
  ExperimentalForeignApi::class,
  BetaInteropApi::class,
)

package net.avianlabs.solana.tweetnacl.ed25519

import kotlinx.cinterop.*
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

public fun PublicKey(data: NSData): PublicKey = PublicKey(
  bytes = ByteArray(data.length.toInt()).apply {
    usePinned { pinned ->
      memcpy(
        __dst = pinned.addressOf(0),
        __src = data.bytes,
        __n = data.length,
      )
    }
  }
)

public val PublicKey.data: NSData
  get() = memScoped {
    NSData.create(
      bytes = allocArrayOf(bytes),
      length = bytes.size.toULong(),
    )
  }
