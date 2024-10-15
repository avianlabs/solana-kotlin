package net.avianlabs.solana.tweetnacl.ed25519

import org.bouncycastle.math.ec.rfc8032.Ed25519

internal actual fun generatePublicKeyBytes(secretKey: ByteArray): ByteArray {
  val bytes = ByteArray(32)
  val pp = Ed25519.generatePublicKey(secretKey, 0)
  Ed25519.encodePublicPoint(pp, bytes, 0)
  return bytes
}
