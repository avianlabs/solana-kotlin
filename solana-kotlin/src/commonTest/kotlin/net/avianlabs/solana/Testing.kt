package net.avianlabs.solana

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.avianlabs.solana.client.RpcKtorClient
import net.avianlabs.solana.domain.core.decode
import net.avianlabs.solana.methods.getTransaction
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class Testing {
  @Test
  fun test() = runTest {
    val client = SolanaClient(RpcKtorClient("https://api.mainnet-beta.solana.com"))
    val tx =
      client.getTransaction("557vAGUJ97dJP1KE4PoKmAp74f246jUuZY4KwR7fYJJrMdBMuda9XJqs4VmzHh8xWoTepDovFZgtFUpS22aZsM1F")

    println(tx)

    val decoded = tx?.decode()
    println(decoded)
  }
}
