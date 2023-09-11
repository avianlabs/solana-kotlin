package net.avianlabs.solana.domain.program

import io.ktor.util.encodeBase64
import kotlinx.coroutines.test.runTest
import net.avianlabs.solana.SolanaClient
import net.avianlabs.solana.client.RpcKtorClient
import net.avianlabs.solana.crypto.Ed25519Keypair
import net.avianlabs.solana.domain.core.Transaction
import net.avianlabs.solana.methods.getMinimumBalanceForRentExemption
import net.avianlabs.solana.methods.getRecentBlockhash
import kotlin.test.Ignore
import kotlin.test.Test

class SystemProgramTest {

  @Test
  @Ignore
  fun testCreateDurableNonceAccount() = runTest {
    val client = SolanaClient(client = RpcKtorClient("https://api.devnet.solana.com"))

    val keypair = Ed25519Keypair.fromBase58("")

    val nonceAccount = Ed25519Keypair.fromBase58("")

    val rentExempt = client.getMinimumBalanceForRentExemption(SystemProgram.NONCE_ACCOUNT_LENGTH)

    val blockhash = client.getRecentBlockhash()

    val transaction = Transaction()
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

    val serialized = transaction.serialize().encodeBase64()

    println(serialized)
  }
}
