package net.avianlabs.solana.tweetnacl.crypto

import com.iwebpp.crypto.TweetNaclFast
import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.ed25519.Ed25519Keypair
import org.bouncycastle.math.ec.rfc8032.Ed25519
import java.security.SecureRandom

internal class JvmCryptoProvider : CryptoProvider {

  private val secureRandom = SecureRandom()

  override fun sign(message: ByteArray, secretKey: ByteArray): ByteArray =
    TweetNaclFast.Signature(ByteArray(0), secretKey).detached(message)

  override fun isOnCurve(publicKey: ByteArray): Boolean =
    Ed25519.validatePublicKeyPartial(publicKey, 0)

  override fun generateKey(seed: ByteArray): Ed25519Keypair {
    val bytes = TweetNaclFast.Signature.keyPair_fromSeed(seed)
    return Ed25519Keypair.fromSecretKeyBytes(bytes.secretKey)
  }

  override fun secretBox(secretKey: ByteArray): TweetNaCl.SecretBox = JvmSecretBox(secretKey)

  override fun secureRandomBytes(count: Int): ByteArray =
    ByteArray(count).also { secureRandom.nextBytes(it) }

  private class JvmSecretBox(private val secretKey: ByteArray) : TweetNaCl.SecretBox {
    override fun box(message: ByteArray, nonce: ByteArray): ByteArray? =
      TweetNaclFast.SecretBox(secretKey).box(message, nonce)

    override fun open(box: ByteArray, nonce: ByteArray): ByteArray? =
      TweetNaclFast.SecretBox(secretKey).open(box, nonce)
  }
}

internal actual fun platformCryptoProvider(): CryptoProvider = JvmCryptoProvider()
