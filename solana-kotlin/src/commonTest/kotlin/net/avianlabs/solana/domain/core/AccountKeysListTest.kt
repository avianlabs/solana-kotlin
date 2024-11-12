package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccountKeysListTest {
  @Test
  fun add_different_writable_flags_preserves_isSigner() {
    val accountKeysList = mutableListOf<AccountMeta>()
    val accountMeta1 = AccountMeta(
      publicKey = PublicKey.fromBase58("4rZoSK72jVaAW1ayZLrefdMPAAStRVhCfH1PSundaoNt"),
      isSigner = false,
      isWritable = true,
    )

    accountKeysList.add(accountMeta1)

    val accountMeta2 = AccountMeta(
      publicKey = PublicKey.fromBase58("4rZoSK72jVaAW1ayZLrefdMPAAStRVhCfH1PSundaoNt"),
      isSigner = true,
      isWritable = false,
    )

    accountKeysList.add(accountMeta2)

    val normalized = accountKeysList.normalize()

    assertTrue(normalized.size == 1)
    assertTrue(normalized[0].isSigner)
    assertTrue(normalized[0].isWritable)
  }

  @Test
  fun account_order() {
    val accountKeysList = mutableListOf<AccountMeta>()

    val meta = listOf(
      AccountMeta(
        publicKey = PublicKey.fromBase58("EtDXsqZ9Cgod7Z6j8cqu8fNMF7fu9txu2puHnxVY1wBk"),
        isSigner = true,
        isWritable = true,
      ),
      AccountMeta(
        publicKey = PublicKey.fromBase58("9JGhZqi4MbnVz424uJ6vqk9a1u359xg3nJekdjzzL4d5"),
        isSigner = false,
        isWritable = true,
      ),
      AccountMeta(
        publicKey = PublicKey.fromBase58("G8iheDY9bGix5qCXEitCExLcgZzZrEemngk9cbTR3CQs"),
        isSigner = false,
        isWritable = false,
      ),
    )

    accountKeysList.addAll(meta.shuffled())

    assertEquals(
      listOf(
        PublicKey.fromBase58("EtDXsqZ9Cgod7Z6j8cqu8fNMF7fu9txu2puHnxVY1wBk"),
        PublicKey.fromBase58("9JGhZqi4MbnVz424uJ6vqk9a1u359xg3nJekdjzzL4d5"),
        PublicKey.fromBase58("G8iheDY9bGix5qCXEitCExLcgZzZrEemngk9cbTR3CQs"),
      ),
      accountKeysList.normalize().map { it.publicKey }
    )
  }
}
