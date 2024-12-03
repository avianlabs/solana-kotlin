package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

internal fun List<AccountMeta>.normalize(
  feePayer: PublicKey? = null,
): List<AccountMeta> = groupBy { it.publicKey }
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
  .sortedWith(metaComparator(feePayer))
  .toList()

private fun metaComparator(feePayer: PublicKey?) = Comparator<AccountMeta> { am1, am2 ->
  // first sort by signer, then writable
  // and ensure feePayer is always first
  if (am1.publicKey == feePayer) {
    -1
  } else if (am2.publicKey == feePayer) {
    1
  } else if (am1.isSigner && !am2.isSigner) {
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
