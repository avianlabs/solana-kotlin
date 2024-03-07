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
  val pk = UByteArray(crypto_sign_ed25519_tweet_PUBLICKEYBYTES)
  val sk = UByteArray(crypto_sign_ed25519_tweet_SECRETKEYBYTES) { i ->
    if (i < crypto_sign_ed25519_tweet_PUBLICKEYBYTES) {
      seed[i].toUByte()
    } else {
      0u
    }
  }
  val res = crypto_sign_ed25519_tweet_keypair(pk.toCValues(), sk.toCValues())
  check(res == 0) {
    "failed to crypto_sign_ed25519_tweet_keypair"
  }
  Ed25519Keypair(
    publicKey = PublicKey(pk.asByteArray()),
    secretKey = sk.asByteArray(),
  )
}

internal actual fun secretBoxInternal(secretKey: ByteArray): TweetNaCl.SecretBox =
  object : TweetNaCl.SecretBox {
    override fun box(message: ByteArray, nonce: ByteArray): ByteArray {
      val boxLength = message.size + crypto_secretbox_xsalsa20poly1305_tweet_ZEROBYTES
      val box = UByteArray(boxLength) { i ->
        if (i >= crypto_secretbox_xsalsa20poly1305_tweet_ZEROBYTES) {
          message[i + crypto_secretbox_xsalsa20poly1305_tweet_ZEROBYTES].toUByte()
        } else {
          0u
        }
      }

      val res = crypto_secretbox_xsalsa20poly1305_tweet(
        message.asUByteArray().toCValues(),
        box.toCValues(),
        message.size.toULong(),
        nonce.asUByteArray().toCValues(),
        secretKey.asUByteArray().toCValues(),
      )

      check(res == 0) {
        "failed to crypto_secretbox_xsalsa20poly1305_tweet"
      }

      return box.asByteArray()
        .sliceArray(crypto_secretbox_xsalsa20poly1305_tweet_ZEROBYTES..<message.size)
    }

    override fun open(box: ByteArray, nonce: ByteArray): ByteArray {
      val cSize = box.size + crypto_secretbox_xsalsa20poly1305_tweet_BOXZEROBYTES
      val c = UByteArray(cSize) { i ->
        if (i >= crypto_secretbox_xsalsa20poly1305_tweet_BOXZEROBYTES) {
          box[i].toUByte()
        } else {
          0u
        }
      }

      val m = UByteArray(cSize)

      val res = crypto_secretbox_xsalsa20poly1305_tweet_open(
        m.toCValues(),
        c.toCValues(),
        c.size.toULong(),
        nonce.asUByteArray().toCValues(),
        secretKey.asUByteArray().toCValues(),
      )

      check(res == 0) {
        "failed to crypto_secretbox_xsalsa20poly1305_tweet_open"
      }

      return c
        .sliceArray(crypto_secretbox_xsalsa20poly1305_tweet_ZEROBYTES..<cSize)
        .asByteArray()
    }
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
