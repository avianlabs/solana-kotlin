package net.avianlabs.solana.tweetnacl

import org.khronos.webgl.Uint8Array

@JsModule("tweetnacl")
internal external val tweetNaclJs: TweetNaClJs

internal external interface TweetNaClJs {
  val secretbox: SecretBox
  val sign: Sign
}

internal external interface Sign {
  fun detached(message: Uint8Array, secretKey: Uint8Array): Uint8Array
  val keyPair: KeyPair
}

internal external interface KeyPair {
  fun fromSeed(seed: Uint8Array): SignKeyPair
}

internal external interface SignKeyPair {
  val secretKey: Uint8Array
  val publicKey: Uint8Array
}

internal external interface SecretBox {
  fun open(box: Uint8Array, nonce: Uint8Array, secretKey: Uint8Array): Uint8Array
}


@Suppress("NOTHING_TO_INLINE")
internal inline operator fun SecretBox.invoke(
  message: Uint8Array,
  nonce: Uint8Array,
  secretKey: Uint8Array
): Uint8Array = asDynamic()(message, nonce, secretKey) as Uint8Array
