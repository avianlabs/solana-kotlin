package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

/**
 * A transaction that supports both legacy and V0 (versioned) message formats.
 *
 * Use [Builder] to construct. When [AddressLookupTableAccount]s are provided,
 * accounts found in them are referenced via lookup indices (V0 format),
 * reducing the on-chain transaction size. When no ALTs are provided,
 * falls back to legacy format.
 */
public class VersionedTransaction internal constructor(
  public val message: VersionedMessage,
  internal val resolvedLookupTables: List<AddressLookupTableAccount>,
) {

  public fun sign(signer: Signer): SignedTransaction = sign(listOf(signer))

  public fun sign(signers: List<Signer>): SignedTransaction {
    val message = when {
      message.feePayer == null && signers.isNotEmpty() -> rebuildWithFeePayer(signers.first().publicKey)
      else -> message
    }

    val serializedMessage = message.serialize()
    val signerKeys = message.staticAccountKeys
      .filter { it.isSigner }
      .map { it.publicKey }

    val signatures = signers.associate { signer ->
      signer.publicKey to
        TweetNaCl.Signature.sign(serializedMessage, signer.secretKey)
    }

    return SignedTransaction(
      serializedMessage = serializedMessage,
      signatures = signatures,
      signerKeys = signerKeys,
    )
  }

  private fun rebuildWithFeePayer(feePayer: PublicKey): VersionedMessage =
    when (val msg = message) {
      is VersionedMessage.Legacy -> VersionedMessage.Legacy(
        msg.message.newBuilder()
          .setFeePayer(feePayer)
          .build()
      )
      is VersionedMessage.V0 -> msg.copy(feePayer = feePayer)
    }

  public class Builder {
    private var feePayer: PublicKey? = null
    private var recentBlockHash: String? = null
    private val instructions = mutableListOf<TransactionInstruction>()
    private val accountKeys = mutableListOf<AccountMeta>()
    private val lookupTableAccounts = mutableListOf<AddressLookupTableAccount>()

    public fun setFeePayer(feePayer: PublicKey): Builder {
      this.feePayer = feePayer
      accountKeys.add(AccountMeta(feePayer, isSigner = true, isWritable = true))
      return this
    }

    public fun setRecentBlockHash(recentBlockHash: String): Builder {
      this.recentBlockHash = recentBlockHash
      return this
    }

    public fun addInstruction(instruction: TransactionInstruction): Builder {
      accountKeys.addAll(
        instruction.keys +
          AccountMeta(instruction.programId, isSigner = false, isWritable = false)
      )
      instructions += instruction
      return this
    }

    public fun addAddressLookupTableAccount(alt: AddressLookupTableAccount): Builder {
      lookupTableAccounts += alt
      return this
    }

    public fun build(): VersionedTransaction {
      if (lookupTableAccounts.isEmpty()) {
        return buildLegacy()
      }
      return buildV0()
    }

    private fun buildLegacy(): VersionedTransaction {
      val normalizedKeys = accountKeys.normalize(feePayer)
      val message = Message.Builder()
        .apply {
          feePayer?.let { setFeePayer(it) }
          recentBlockHash?.let { setRecentBlockHash(it) }
          instructions.forEach { addInstruction(it) }
        }
        .build()
      return VersionedTransaction(
        message = VersionedMessage.Legacy(message),
        resolvedLookupTables = emptyList(),
      )
    }

    private fun buildV0(): VersionedTransaction {
      val normalizedKeys = accountKeys.normalize(feePayer)

      // Build ALT index: publicKey → (ALT, indexInALT)
      val altIndex = mutableMapOf<PublicKey, Pair<AddressLookupTableAccount, Int>>()
      for (alt in lookupTableAccounts) {
        for ((index, address) in alt.addresses.withIndex()) {
          // First ALT wins if address appears in multiple
          if (address !in altIndex) {
            altIndex[address] = alt to index
          }
        }
      }

      // Collect program IDs — they must always be static
      val programIds = instructions.map { it.programId }.toSet()

      // Partition accounts into static vs lookupable
      val staticKeys = mutableListOf<AccountMeta>()
      val lookupableWritable = mutableMapOf<PublicKey, MutableList<Pair<PublicKey, Int>>>()
      val lookupableReadonly = mutableMapOf<PublicKey, MutableList<Pair<PublicKey, Int>>>()

      for (meta in normalizedKeys) {
        val key = meta.publicKey
        val altEntry = altIndex[key]

        // Static if: signer, program ID, or not in any ALT
        if (meta.isSigner || key in programIds || altEntry == null) {
          staticKeys.add(meta)
        } else {
          val (alt, indexInAlt) = altEntry
          if (meta.isWritable) {
            lookupableWritable.getOrPut(alt.key) { mutableListOf() }
              .add(key to indexInAlt)
          } else {
            lookupableReadonly.getOrPut(alt.key) { mutableListOf() }
              .add(key to indexInAlt)
          }
        }
      }

      // Build address table lookups
      val allAltKeys = (lookupableWritable.keys + lookupableReadonly.keys).distinct()
      val addressTableLookups = allAltKeys.map { altKey ->
        MessageAddressTableLookup(
          accountKey = altKey,
          writableIndexes = lookupableWritable[altKey]
            ?.map { it.second.toUByte() } ?: emptyList(),
          readonlyIndexes = lookupableReadonly[altKey]
            ?.map { it.second.toUByte() } ?: emptyList(),
        )
      }

      val v0Message = VersionedMessage.V0(
        feePayer = feePayer,
        recentBlockHash = recentBlockHash,
        staticAccountKeys = staticKeys,
        instructions = instructions,
        addressTableLookups = addressTableLookups,
        resolvedLookupTables = lookupTableAccounts,
      )

      return VersionedTransaction(
        message = v0Message,
        resolvedLookupTables = lookupTableAccounts,
      )
    }
  }
}
