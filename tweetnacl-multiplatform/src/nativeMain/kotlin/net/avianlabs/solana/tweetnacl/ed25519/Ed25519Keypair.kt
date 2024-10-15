@file:OptIn(ExperimentalForeignApi::class)

package net.avianlabs.solana.tweetnacl.ed25519

import kotlinx.cinterop.*
import net.avianlabs.solana.tweetnacl.crypto_sign_ed25519_tweet_PUBLICKEYBYTES
import net.avianlabs.solana.tweetnacl.public_key_from_secret
import net.avianlabs.solana.tweetnacl.toByteArray

@OptIn(ExperimentalForeignApi::class)
internal actual fun generatePublicKeyBytes(secretKey: ByteArray): ByteArray = memScoped {
  val pkPointer = allocArray<UByteVar>(crypto_sign_ed25519_tweet_PUBLICKEYBYTES)

  public_key_from_secret(secretKey.asUByteArray().toCValues(), pkPointer)

  pkPointer.toByteArray(crypto_sign_ed25519_tweet_PUBLICKEYBYTES)
}
