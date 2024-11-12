package net.avianlabs.solana.domain.core

internal fun List<AccountMeta>.normalize(): List<AccountMeta> = groupBy { it.publicKey }
  .mapValues { (_, metas) ->
    metas.reduce { acc, meta ->
      AccountMeta(
        publicKey = acc.publicKey,
        isSigner = acc.isSigner || meta.isSigner,
        isWritable = acc.isWritable || meta.isWritable,
      )
    }
  }
  .values
  .sortedWith(metaComparator)
  .toList()

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
