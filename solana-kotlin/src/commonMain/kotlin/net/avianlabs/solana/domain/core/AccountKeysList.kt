package net.avianlabs.solana.domain.core

public class AccountKeysList {
  private val accounts: LinkedHashMap<String, AccountMeta> = LinkedHashMap()

  public fun add(accountMeta: AccountMeta) {
    val key = accountMeta.publicKey.toString()
    val existing = accounts[key]
    if (existing != null) {
      accounts[key] = existing.copy(
        isSigner = accountMeta.isSigner || existing.isSigner,
        isWritable = accountMeta.isWritable || existing.isWritable,
      )
    } else {
      accounts[key] = accountMeta
    }
  }

  public fun addAll(metas: Collection<AccountMeta>) {
    for (meta in metas) {
      add(meta)
    }
  }

  public val list: ArrayList<AccountMeta>
    get() {
      val accountKeysList = ArrayList(accounts.values)
      accountKeysList.sortWith(metaComparator)
      return accountKeysList
    }

  public companion object {
    private val metaComparator = Comparator<AccountMeta> { am1, am2 ->
      // first sort by signer, then writable
      if (am1.isSigner && !am2.isSigner) {
        -1
      } else if (!am1.isSigner && am2.isSigner) {
        1
      } else if (am1.isWritable && !am2.isWritable) {
        -1
      } else if (!am1.isWritable && am2.isWritable) {
        1
      } else {
        0
      }
    }
  }
}
