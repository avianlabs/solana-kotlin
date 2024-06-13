package net.avianlabs.solana.tweetnacl

import net.avianlabs.solana.tweetnacl.ed25519.Ed25519Keypair
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get

internal actual fun signInternal(message: ByteArray, secretKey: ByteArray): ByteArray =
  tweetNaclJs.sign.detached(message.asUint8Array(), secretKey.asUint8Array()).asByteArray()

internal actual fun isOnCurveInternal(publicKey: ByteArray): Boolean =
  try {
    Curves.ed25519.extendedPoint.fromHex(publicKey.asUint8Array())
    true
  } catch (e: Throwable) {
    false
  }

internal actual fun generateKeyInternal(seed: ByteArray): Ed25519Keypair {
  val bytes = tweetNaclJs.sign.keyPair.fromSeed(seed.asUint8Array())
  return Ed25519Keypair.fromSecretKeyBytes(bytes.secretKey.asByteArray())
}

internal actual fun secretBoxInternal(secretKey: ByteArray): TweetNaCl.SecretBox =
  object : TweetNaCl.SecretBox {
    override fun box(message: ByteArray, nonce: ByteArray): ByteArray {
      return tweetNaclJs.secretbox(
        message.asUint8Array(),
        nonce.asUint8Array(),
        secretKey.asUint8Array()
      ).asByteArray()
    }

    override fun open(box: ByteArray, nonce: ByteArray): ByteArray {
      return tweetNaclJs.secretbox.open(
        box.asUint8Array(),
        nonce.asUint8Array(),
        secretKey.asUint8Array()
      ).asByteArray()
    }
  }

private fun ByteArray.asUint8Array(): Uint8Array = Uint8Array(this.toTypedArray())

private fun Uint8Array.asByteArray(): ByteArray = ByteArray(this.length) { this[it] }
