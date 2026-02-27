package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

/**
 * A Solana transaction message that can be either legacy format or versioned (V0).
 *
 * Legacy messages use a flat account key list. V0 messages additionally reference
 * on-chain address lookup tables, allowing transactions to include more accounts
 * than the legacy limit of ~35.
 */
public sealed class VersionedMessage {
  public abstract val feePayer: PublicKey?
  public abstract val recentBlockHash: String?
  public abstract val staticAccountKeys: List<AccountMeta>
  public abstract val instructions: List<TransactionInstruction>

  /**
   * Wraps an existing [Message] as a legacy versioned message.
   */
  public data class Legacy(public val message: Message) : VersionedMessage() {
    override val feePayer: PublicKey? get() = message.feePayer
    override val recentBlockHash: String? get() = message.recentBlockHash
    override val staticAccountKeys: List<AccountMeta> get() = message.accountKeys
    override val instructions: List<TransactionInstruction> get() = message.instructions
  }

  /**
   * A version 0 message that supports address lookup tables.
   *
   * [addressTableLookups] reference on-chain ALTs and the indices within them
   * that should be loaded as additional transaction accounts.
   *
   * [resolvedLookupTables] holds the full resolved ALT data needed during serialization
   * to compute instruction account indices. Populated by [VersionedTransaction.Builder].
   */
  public data class V0(
    override val feePayer: PublicKey?,
    override val recentBlockHash: String?,
    override val staticAccountKeys: List<AccountMeta>,
    override val instructions: List<TransactionInstruction>,
    public val addressTableLookups: List<MessageAddressTableLookup>,
    internal val resolvedLookupTables: List<AddressLookupTableAccount> = emptyList(),
  ) : VersionedMessage()
}
