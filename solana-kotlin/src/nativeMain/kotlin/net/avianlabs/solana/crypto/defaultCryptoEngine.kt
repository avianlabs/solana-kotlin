@file:OptIn(ExperimentalForeignApi::class)

package net.avianlabs.solana.crypto

import kotlinx.cinterop.*
import net.avianlabs.solana.domain.core.PublicKey
import net.avianlabs.solana.tweetnacl.*

internal actual val defaultCryptoEngine: CryptoEngine = object : CryptoEngine {
  override fun sign(message: ByteArray, secretKey: ByteArray): ByteArray = memScoped {
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

  override fun isOnCurve(publicKey: ByteArray): Boolean =
    is_on_curve(publicKey.toUByteArray().toCValues()) != -1

  override fun generateKey(seed: ByteArray): Ed25519Keypair = memScoped {
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
