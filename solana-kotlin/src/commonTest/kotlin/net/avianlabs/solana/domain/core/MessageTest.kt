package net.avianlabs.solana.domain.core

import net.avianlabs.solana.domain.program.ComputeBudgetProgram
import net.avianlabs.solana.domain.program.TokenProgram
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageTest {

  @Test
  fun account_keys_order() {
    val transaction = Transaction()
      .addInstruction(
        TokenProgram.transferChecked(
          source = PublicKey.fromBase58("9JGhZqi4MbnVz424uJ6vqk9a1u359xg3nJekdjzzL4d5"),
          mint = PublicKey.fromBase58("G8iheDY9bGix5qCXEitCExLcgZzZrEemngk9cbTR3CQs"),
          destination = PublicKey.fromBase58("3RfWpJnpr4DHxMDWfhqZRQ4acnQwM1HQXSvmgzzpz2K2"),
          owner = PublicKey.fromBase58("EtDXsqZ9Cgod7Z6j8cqu8fNMF7fu9txu2puHnxVY1wBk"),
          amount = 1u,
          decimals = 1u,
        )
      )
      .addInstruction(
        ComputeBudgetProgram.setComputeUnitPrice(
          microLamports = 1u,
        )
      )

    transaction.message.accountKeys.map { println(it) }

    assertEquals(
      transaction.message.accountKeys,
      listOf(
        AccountMeta(
          publicKey = PublicKey.fromBase58("EtDXsqZ9Cgod7Z6j8cqu8fNMF7fu9txu2puHnxVY1wBk"),
          isSigner = true,
          isWritable = false
        ),
        AccountMeta(
          publicKey = PublicKey.fromBase58("9JGhZqi4MbnVz424uJ6vqk9a1u359xg3nJekdjzzL4d5"),
          isSigner = false,
          isWritable = true
        ),
        AccountMeta(
          publicKey = PublicKey.fromBase58("3RfWpJnpr4DHxMDWfhqZRQ4acnQwM1HQXSvmgzzpz2K2"),
          isSigner = false,
          isWritable = true
        ),
        AccountMeta(
          publicKey = PublicKey.fromBase58("G8iheDY9bGix5qCXEitCExLcgZzZrEemngk9cbTR3CQs"),
          isSigner = false,
          isWritable = false
        ),
        AccountMeta(
          publicKey = PublicKey.fromBase58("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"),
          isSigner = false,
          isWritable = false
        ),
        AccountMeta(
          publicKey = PublicKey.fromBase58("ComputeBudget111111111111111111111111111111"),
          isSigner = false,
          isWritable = false
        ),
      ),
    )
  }
}
