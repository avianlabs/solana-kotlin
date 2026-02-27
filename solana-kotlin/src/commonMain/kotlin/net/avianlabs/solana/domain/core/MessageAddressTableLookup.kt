package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

/**
 * References an on-chain address lookup table and the writable/readonly indices into it
 * that should be loaded for the transaction.
 */
public data class MessageAddressTableLookup(
  public val accountKey: PublicKey,
  public val writableIndexes: List<UByte>,
  public val readonlyIndexes: List<UByte>,
)
