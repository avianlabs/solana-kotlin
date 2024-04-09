package net.avianlabs.solana.tweetnacl

import org.khronos.webgl.Uint8Array

@JsModule("@noble/curves/ed25519")
internal external object Curves {
  val ed25519: Ed25519
}

internal external interface Ed25519 {
  @JsName("ExtendedPoint")
  val extendedPoint: ExtendedPoint
}

internal external interface ExtendedPoint {
  fun fromHex(publicKey: Uint8Array): dynamic
}
