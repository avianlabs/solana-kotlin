package net.avianlabs.solana.domain.program

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcKtorClient
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.domain.core.Transaction
import net.avianlabs.solana.domain.core.TransactionBuilder
import net.avianlabs.solana.domain.core.decode
import net.avianlabs.solana.methods.*
import net.avianlabs.solana.tweetnacl.TweetNaCl
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class SystemProgramTest {

  @Test
  @Ignore
  fun testCreateDurableNonceAccount() = runBlocking {
    val client = SolanaClient(client = RpcKtorClient("http://localhost:8899"))

    val keypair = TweetNaCl.Signature.generateKey(Random.nextBytes(32))
    println("Keypair: ${keypair.publicKey}")
    val nonceAccount = TweetNaCl.Signature.generateKey(Random.nextBytes(32))
    println("Nonce account: ${nonceAccount.publicKey}")

    client.requestAirdrop(keypair.publicKey, 2_000_000_000)
    delay(15.seconds)
    val balance = client.getBalance(keypair.publicKey)
    println("Balance: $balance")

    val rentExempt = client.getMinimumBalanceForRentExemption(SystemProgram.NONCE_ACCOUNT_LENGTH)

    val blockhash = client.getRecentBlockhash()

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


    val initSignature = client.sendTransaction(initTransaction)

    println("Initialized nonce account: $initSignature")
    delay(15.seconds)

    val lamportsPerSignature = client.getFeeForMessage(initTransaction.message.serialize())
    println("Lamports per signature: $lamportsPerSignature")

    val nonce = client.getNonce(nonceAccount.publicKey, Commitment.Processed)
    println("Nonce account info: $nonce")

    val testTransaction = TransactionBuilder()
      .addInstruction(
        SystemProgram.nonceAdvance(
          nonceAccount = nonceAccount.publicKey,
          authorized = keypair.publicKey,
        )
      )
      .addInstruction(
        SystemProgram.transfer(
          fromPublicKey = keypair.publicKey,
          toPublicKey = nonceAccount.publicKey,
          lamports = 1_000_000_000,
        )
      )
      .setRecentBlockHash(nonce!!.nonce)
      .build()
      .sign(keypair)

    val testSignature = client.sendTransaction(testTransaction)
    println("Advanced nonce account: $testSignature")

    delay(15.seconds)

    val testTxInfo = client.getTransaction(testSignature, Commitment.Confirmed)
    println("Transaction info: ${testTxInfo?.decode()}")

    val newNonce = client.getNonce(nonceAccount.publicKey, Commitment.Processed)
    println("New nonce account info: $newNonce")
  }
}
