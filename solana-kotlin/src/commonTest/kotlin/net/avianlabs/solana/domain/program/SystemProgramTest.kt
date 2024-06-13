package net.avianlabs.solana.domain.program

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcKtorClient
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.domain.core.Transaction
import net.avianlabs.solana.domain.core.TransactionBuilder
import net.avianlabs.solana.methods.*
import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.ed25519.Ed25519Keypair
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test

private val logger = KotlinLogging.logger { }

class SystemProgramTest {

  @Ignore
  @Test
  fun testCreateDurableNonceAccount() = runTest {
    val client = SolanaClient(client = RpcKtorClient("http://localhost:8899"))
    val owner = TweetNaCl.Signature.generateKey(Random.nextBytes(32))
    logger.info { "Keypair: ${owner.publicKey}" }
    val nonce1 = TweetNaCl.Signature.generateKey(Random.nextBytes(32))
    logger.info { "Nonce account 1: ${nonce1.publicKey}" }
    val nonce2 = TweetNaCl.Signature.generateKey(Random.nextBytes(32))
    logger.info { "Nonce account 2: ${nonce2.publicKey}" }
    val destination = TweetNaCl.Signature.generateKey(Random.nextBytes(32))
    logger.info { "Destination: ${destination.publicKey}" }

    val airdrop = client.requestAirdrop(owner.publicKey, 2_000_000_000)
    val airdrop2 = client.requestAirdrop(destination.publicKey, 2_000_000_000)
    client.waitForTransaction(airdrop, airdrop2)

    val balance = client.getBalance(owner.publicKey)
    logger.info { "Balance: $balance" }

    val nonce1tx = client.createNonceAccountSigned(
      keypair = owner,
      nonceAccount = nonce1,
    )
    val init1Sig = client.sendTransaction(nonce1tx)

    logger.info { "Initialized nonce account 1: $init1Sig" }

    val nonce2tx = client.createNonceAccountSigned(
      keypair = owner,
      nonceAccount = nonce2,
    )

    val init2Sig = client.sendTransaction(nonce2tx)

    logger.info { "Initialized nonce account 2: $init2Sig" }

    client.waitForTransaction(init1Sig, init2Sig)

    val nonceAccount1 = client.getNonce(nonce1.publicKey, Commitment.Confirmed)
    logger.info { "Nonce account 1 info: $nonceAccount1" }

    val tx1 = transferTransaction(
      nonceKeypair = nonce1,
      owner = owner,
      destination = destination,
      nonceAccount = nonceAccount1,
    )

    val nonceAccount2 = client.getNonce(nonce2.publicKey, Commitment.Confirmed)
    logger.info { "Nonce account 2 info: $nonceAccount2" }

    val tx2 = transferTransaction(
      nonceKeypair = nonce2,
      owner = owner,
      destination = destination,
      nonceAccount = nonceAccount2,
    )

    val tx2Sig = client.sendTransaction(tx2)

    client.waitForTransaction(tx2Sig)

    val tx1Sig = client.sendTransaction(tx1)
    logger.info { "Advanced nonce account: $tx1Sig" }

    client.waitForTransaction(tx1Sig)

    val newNonce = client.getNonce(nonce1.publicKey, Commitment.Confirmed)
    logger.info { "New nonce: ${newNonce?.nonce}" }
  }

  private fun transferTransaction(
    nonceKeypair: Ed25519Keypair,
    owner: Ed25519Keypair,
    destination: Ed25519Keypair,
    nonceAccount: NonceAccount?
  ) = TransactionBuilder()
    .addInstruction(
      SystemProgram.nonceAdvance(
        nonceAccount = nonceKeypair.publicKey,
        authorized = owner.publicKey,
      )
    )
    .addInstruction(
      SystemProgram.transfer(
        fromPublicKey = owner.publicKey,
        toPublicKey = destination.publicKey,
        lamports = 1_000,
      )
    )
    .setRecentBlockHash(nonceAccount!!.nonce)
    .build()
    .sign(owner)

  private suspend fun SolanaClient.createNonceAccountSigned(
    keypair: Ed25519Keypair,
    nonceAccount: Ed25519Keypair
  ): Transaction {
    val rentExempt = getMinimumBalanceForRentExemption(SystemProgram.NONCE_ACCOUNT_LENGTH)

    val blockhash = getRecentBlockhash()

    val initTransaction = Transaction()
      .addInstruction(
        SystemProgram.createAccount(
          fromPublicKey = keypair.publicKey,
          newAccountPublicKey = nonceAccount.publicKey,
          lamports = rentExempt,
          space = SystemProgram.NONCE_ACCOUNT_LENGTH,
        )
      )
      .addInstruction(
        SystemProgram.nonceInitialize(
          nonceAccount = nonceAccount.publicKey,
          authorized = keypair.publicKey,
        )
      )
      .setRecentBlockHash(blockhash.blockhash)
      .sign(listOf(keypair, nonceAccount))
    return initTransaction
  }

  private suspend fun SolanaClient.waitForTransaction(
    vararg signatures: String,
    commitment: Commitment = Commitment.Finalized,
  ) = coroutineScope {
    signatures.map { signature ->
      async {
        var transactionResponse: TransactionResponse?
        do {
          transactionResponse = getTransaction(signature, commitment)
        } while (transactionResponse == null)
        logger.info { "Transaction $commitment $signature" }
      }
    }.awaitAll()
  }
}
