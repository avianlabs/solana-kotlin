package net.avianlabs.solana.domain.core

import kotlin.test.Test
import kotlin.test.assertTrue

class AccountKeysListTest {
  @Test
  fun add_different_writable_flags_preserves_isSigner() {
    val accountKeysList = AccountKeysList()
    val accountMeta1 = AccountMeta(PublicKey.fromBase58("4rZoSK72jVaAW1ayZLrefdMPAAStRVhCfH1PSundaoNt"), false, true)

    accountKeysList.add(accountMeta1)

    val accountMeta2 = AccountMeta(PublicKey.fromBase58("4rZoSK72jVaAW1ayZLrefdMPAAStRVhCfH1PSundaoNt"), true, false)

    accountKeysList.add(accountMeta2)

    assertTrue(accountKeysList.list.size == 1)
    assertTrue(accountKeysList.list[0].isSigner)
    assertTrue(accountKeysList.list[0].isWritable)
  }
}
