package net.avianlabs.solana.methods

import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.domain.core.FeeCalculator
import net.avianlabs.solana.domain.core.NonceAccountData
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import okio.Buffer

public suspend fun SolanaClient.getNonce(
  publicKey: PublicKey,
  commitment: Commitment? = null
): NonceAccount? = getAccountInfo(publicKey, commitment)
  .result
  ?.value
  ?.dataBytes
  ?.let { data ->
    val decoded = NonceAccountData.read(Buffer().write(data))
    NonceAccount(
      authorizedPubkey = decoded.authorizedPubkey,
      nonce = decoded.nonce,
      feeCalculator = decoded.feeCalculator,
    )
  }

public data class NonceAccount(
  val authorizedPubkey: PublicKey,
  val nonce: String,
  val feeCalculator: FeeCalculator,
)
