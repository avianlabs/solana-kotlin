package net.avianlabs.solana.crypto

import com.iwebpp.crypto.TweetNaclFast
import org.bouncycastle.math.ec.rfc8032.Ed25519

internal actual val defaultCryptoEngine: CryptoEngine = object : CryptoEngine {
  override fun sign(message: ByteArray, secretKey: ByteArray): ByteArray {
    val signatureProvider = TweetNaclFast.Signature(ByteArray(0), secretKey)
    return signatureProvider.detached(message)
  }

  override fun isOnCurve(publicKey: ByteArray): Boolean =
    Ed25519.validatePublicKeyPartial(publicKey, 0)

  override fun generateKey(seed: ByteArray): Ed25519Keypair {
    val bytes = TweetNaclFast.Signature.keyPair_fromSeed(seed)
    return Ed25519Keypair.fromSecretKeyBytes(bytes.secretKey)
  }
}
