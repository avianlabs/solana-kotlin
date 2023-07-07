package net.avianlabs.solana.crypto

import net.avianlabs.solana.domain.core.PublicKey
import net.avianlabs.solana.vendor.TweetNaclFast

internal actual val defaultCryptoEngine: CryptoEngine = object : CryptoEngine {
  override fun sign(message: ByteArray, secretKey: ByteArray): ByteArray {
    val signatureProvider = TweetNaclFast.Signature(ByteArray(0), secretKey)
    return signatureProvider.detached(message)
  }

  override fun isOnCurve(publicKey: ByteArray): Boolean = TweetNaclFast.is_on_curve(publicKey) != 0

  override fun generateKey(): Ed25519Keypair {
    val keypair = TweetNaclFast.Signature.keyPair()
    return Ed25519Keypair(
      publicKey = PublicKey(keypair.publicKey),
      secretKey = keypair.secretKey,
    )
  }
}
