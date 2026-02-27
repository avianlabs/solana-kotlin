package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

/**
 * A resolved address lookup table containing the table's on-chain address
 * and its full list of addresses. Used when building versioned transactions
 * to determine which accounts can be referenced via the table.
 */
public data class AddressLookupTableAccount(
  public val key: PublicKey,
  public val addresses: List<PublicKey>,
)
