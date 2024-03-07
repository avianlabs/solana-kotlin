package net.avianlabs.solana.tweetnacl

import com.iwebpp.crypto.TweetNaclFast
import net.avianlabs.solana.crypto.Ed25519Keypair
import org.bouncycastle.math.ec.rfc8032.Ed25519


internal actual fun signInternal(message: ByteArray, secretKey: ByteArray): ByteArray {
  val signatureProvider = TweetNaclFast.Signature(ByteArray(0), secretKey)
  return signatureProvider.detached(message)
}

internal actual fun isOnCurveInternal(publicKey: ByteArray): Boolean =
  Ed25519.validatePublicKeyPartial(publicKey, 0)

internal actual fun generateKeyInternal(seed: ByteArray): Ed25519Keypair {
  val bytes = TweetNaclFast.Signature.keyPair_fromSeed(seed)
  return Ed25519Keypair.fromSecretKeyBytes(bytes.secretKey)
}
