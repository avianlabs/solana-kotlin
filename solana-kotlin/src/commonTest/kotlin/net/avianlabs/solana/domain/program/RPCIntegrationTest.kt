package net.avianlabs.solana.domain.program

import io.ktor.client.*
import io.ktor.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcKtorClient
import net.avianlabs.solana.domain.core.Commitment
import net.avianlabs.solana.domain.core.Transaction
import net.avianlabs.solana.domain.core.decode
import net.avianlabs.solana.domain.core.serialize
import net.avianlabs.solana.methods.*
import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

/**
 *  Connects to a local Solana node. This test is ignored in CI.
 *
 *  Run with solana-test-validator --ticks-per-slot 3
 */
@Ignore
class RPCIntegrationTest {

  private val client = SolanaClient(
    client = RpcKtorClient(
      "http://localhost:8899",
      httpClient = HttpClient {}
    ),
  )

  @Test
  fun testCreateDurableNonceAccount() = runBlocking {
    val keypair = TweetNaCl.Signature.generateKey(Random.nextBytes(32))
    println("Keypair: ${keypair.publicKey}")
    val nonceAccount = TweetNaCl.Signature.generateKey(Random.nextBytes(32))
    println("Nonce account: ${nonceAccount.publicKey}")

    client.requestAirdrop(keypair.publicKey, 2_000_000_000)

    delay(1.seconds)
    val balance = client.getBalance(keypair.publicKey)
    println("Balance: $balance")

    val rentExempt =
      client.getMinimumBalanceForRentExemption(SystemProgram.NONCE_LENGTH).result!!

    val blockhash = client.getLatestBlockhash().result!!.value

    val initTransaction = Transaction.Builder()
      .addInstruction(
        SystemProgram.createAccount(
          newAccount = nonceAccount.publicKey,
          lamports = rentExempt.toULong(),
          space = SystemProgram.NONCE_LENGTH.toULong(),
          programAddress = SystemProgram.programId,
        )
      )
      .addInstruction(
        SystemProgram.initializeNonceAccount(
          nonceAccount = nonceAccount.publicKey,
          nonceAuthority = keypair.publicKey,
        )
      )
      .setRecentBlockHash(blockhash.blockhash)
      .build()
      .sign(listOf(keypair, nonceAccount))


    val simulated = client.simulateTransaction(initTransaction)

    println("simulated: $simulated")

    val initSignature = client.sendTransaction(initTransaction)

    println("Initialized nonce account: $initSignature")
    delay(1.seconds)

    val lamportsPerSignature = client.getFeeForMessage(initTransaction.message.serialize())
    println("Lamports per signature: $lamportsPerSignature")

    val nonce = client.getNonce(nonceAccount.publicKey, Commitment.Processed)
    println("Nonce account info: $nonce")

    val testTransaction = Transaction.Builder()
      .addInstruction(
        SystemProgram.advanceNonceAccount(
          nonceAccount = nonceAccount.publicKey,
          nonceAuthority = keypair.publicKey,
        )
      )
      .addInstruction(
        SystemProgram.transferSol(
          source = keypair.publicKey,
          destination = nonceAccount.publicKey,
          amount = 1_000_000_000UL,
        )
      )
      .setRecentBlockHash(nonce!!.nonce)
      .build()
      .sign(keypair)

    val testSignature = client.sendTransaction(testTransaction).result!!
    println("Advanced nonce account: $testSignature")

    delay(1.seconds)

    val testTxInfo = client.getTransaction(testSignature, Commitment.Confirmed).result
    println("Transaction info: ${testTxInfo?.decode()}")

    val newNonce = client.getNonce(nonceAccount.publicKey, Commitment.Processed)
    println("New nonce account info: $newNonce")
  }

  @Test
  fun testSimulateTransaction() = runBlocking {
    val keypair = TweetNaCl.Signature.generateKey(Random.nextBytes(32))
    println("Keypair: ${keypair.publicKey}")
    val initTransaction = Transaction.Builder()
      .addInstruction(
        SystemProgram.transferSol(
          keypair.publicKey,
          PublicKey.fromBase58("11111111111111111111111111111111"),
          1UL,
        )
      )

    val toSimulate = initTransaction
      .setRecentBlockHash("11111111111111111111111111111111")
      .setFeePayer(keypair.publicKey)
      .build()


    val serialized = toSimulate.sign(emptyList()).serialize()
    println("Serialized: ${serialized.encodeBase64()}")

    val simulated =
      client.simulateTransaction(toSimulate, sigVerify = false, replaceRecentBlockhash = true)

    println("simulated: $simulated")
  }
}
