@file:OptIn(ExperimentalForeignApi::class)

package net.avianlabs.solana.tweetnacl

import kotlinx.cinterop.*
import net.avianlabs.solana.tweetnacl.ed25519.Ed25519Keypair
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

internal actual fun signInternal(message: ByteArray, secretKey: ByteArray): ByteArray = memScoped {
  check(secretKey.size == crypto_sign_ed25519_tweet_SECRETKEYBYTES) {
    "secretKey size is ${secretKey.size} bytes, should be $crypto_sign_ed25519_tweet_SECRETKEYBYTES bytes"
  }

  val signedLenght = message.size + crypto_sign_ed25519_tweet_BYTES
  val signedMessage = allocArray<UByteVar>(signedLenght)
  val tmpLength = allocArray<ULongVar>(1)

  val res = crypto_sign_ed25519_tweet(
    signedMessage,
    tmpLength,
    message.toUByteArray().toCValues(),
    message.size.toULong(),
    secretKey.toUByteArray().toCValues(),
  )

  check(res == 0) {
    "failed to crypto_sign_ed25519_tweet"
  }

  signedMessage.toByteArray(signedLenght)
}.sliceArray(0..<crypto_sign_ed25519_tweet_BYTES)


internal actual fun isOnCurveInternal(publicKey: ByteArray): Boolean =
  is_on_curve(publicKey.toUByteArray().toCValues()) != -1

internal actual fun generateKeyInternal(seed: ByteArray): Ed25519Keypair = memScoped {
  val pkPointer = allocArray<UByteVar>(crypto_sign_ed25519_tweet_PUBLICKEYBYTES)
  val skPointer = allocArray<UByteVar>(crypto_sign_ed25519_tweet_SECRETKEYBYTES) { i ->
    if (i < crypto_sign_ed25519_tweet_PUBLICKEYBYTES)
      this.value = seed[i].toUByte()
  }
  val res = crypto_sign_ed25519_tweet_keypair(pkPointer, skPointer)
  check(res == 0) {
    "failed to crypto_sign_ed25519_tweet_keypair"
  }
  Ed25519Keypair(
    publicKey = PublicKey(pkPointer.toByteArray(crypto_sign_ed25519_tweet_PUBLICKEYBYTES)),
    secretKey = skPointer.toByteArray(crypto_sign_ed25519_tweet_SECRETKEYBYTES),
  )
}

private fun CPointer<UByteVar>.toByteArray(length: Int): ByteArray {
  val nativeBytes = this
  val bytes = ByteArray(length)
  var index = 0
  while (index < length) {
    bytes[index] = nativeBytes[index].toByte()
    ++index
  }
  return bytes
}
