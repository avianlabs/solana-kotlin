package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import net.avianlabs.solana.tweetnacl.vendor.encodeToBase58String
import net.avianlabs.solana.vendor.ShortVecEncoding
import okio.Buffer

/**
 * A transaction that supports both legacy and V0 (versioned) message formats.
 *
 * Use [Builder] to construct. When [AddressLookupTableAccount]s are provided,
 * accounts found in them are referenced via lookup indices (V0 format),
 * reducing the on-chain transaction size. When no ALTs are provided,
 * falls back to legacy format.
 *
 * Signing returns a new [VersionedTransaction] instance (immutable), so the
 * same type flows through build → sign → serialize without type changes.
 */
public class VersionedTransaction internal constructor(
  public val message: VersionedMessage,
  public val signatures: Map<PublicKey, ByteArray>,
  rawSerializedMessage: ByteArray?,
) {

  /**
   * Creates a [VersionedTransaction] with the given [message] and optional [signatures].
   *
   * The [serializedMessage] is lazily computed from [message] on first access.
   */
  public constructor(
    message: VersionedMessage,
    signatures: Map<PublicKey, ByteArray> = emptyMap(),
  ) : this(message, signatures, null)

  /**
   * The public keys of all accounts that must sign this transaction,
   * derived from the message's static account keys.
   */
  public val signerKeys: List<PublicKey>
    get() = message.staticAccountKeys
      .filter { it.isSigner }
      .map { it.publicKey }

  /**
   * The binary wire-format bytes of the [message].
   *
   * When deserialized from raw bytes, the original bytes are preserved directly.
   * Otherwise, lazily computed from [message] on first access.
   */
  public val serializedMessage: ByteArray by lazy {
    rawSerializedMessage ?: message.serialize()
  }

  /**
   * Signs this transaction with a single signer, returning a new
   * [VersionedTransaction] with the accumulated signatures.
   *
   * If no fee payer has been set, the signer's public key is used.
   */
  public fun sign(signer: Signer): VersionedTransaction = sign(listOf(signer))

  /**
   * Signs this transaction with multiple signers, returning a new
   * [VersionedTransaction] with the accumulated signatures.
   *
   * If no fee payer has been set, the first signer's public key is used.
   */
  public fun sign(signers: List<Signer>): VersionedTransaction {
    val actualMessage = when {
      message.feePayer == null && signers.isNotEmpty() ->
        rebuildWithFeePayer(signers.first().publicKey)

      else -> message
    }

    val msgBytes =
      if (actualMessage === message) serializedMessage else actualMessage.serialize()
    val newSignatures = signers.associate { signer ->
      signer.publicKey to TweetNaCl.Signature.sign(msgBytes, signer.secretKey)
    }

    return VersionedTransaction(
      message = actualMessage,
      signatures = this.signatures + newSignatures,
      rawSerializedMessage = msgBytes,
    )
  }

  /**
   * Serializes this transaction to its binary wire format.
   *
   * All signer slots are always included in the output, with zero-filled
   * placeholders for any signers that have not yet signed.
   */
  public fun serialize(): SerializedTransaction {
    val orderedSigs = signerKeys.map { key ->
      signatures[key] ?: ByteArray(TweetNaCl.Signature.SIGNATURE_BYTES)
    }
    val signaturesLength = ShortVecEncoding.encodeLength(orderedSigs.size)
    val msgBytes = serializedMessage
    val bufferSize =
      signaturesLength.size +
        orderedSigs.size * TweetNaCl.Signature.SIGNATURE_BYTES +
        msgBytes.size
    val out = Buffer()
    out.write(signaturesLength)
    orderedSigs.forEach(out::write)
    out.write(msgBytes)
    return SerializedTransaction(out.readByteArray(bufferSize.toLong()))
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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as VersionedTransaction

    if (message != other.message) return false
    if (signerKeys != other.signerKeys) return false
    if (signatures.size != other.signatures.size) return false
    for ((key, value) in signatures) {
      val otherValue = other.signatures[key] ?: return false
      if (!value.contentEquals(otherValue)) return false
    }

    return true
  }

  override fun hashCode(): Int {
    var result = message.hashCode()
    result = 31 * result + signerKeys.hashCode()
    result = 31 * result + signatures.entries.fold(0) { acc, (k, v) ->
      acc + k.hashCode() + v.contentHashCode()
    }
    return result
  }

  override fun toString(): String =
    "VersionedTransaction(signatures=${signatures.values.map { it.encodeToBase58String() }})"

  public companion object {
    /**
     * Deserializes a [VersionedTransaction] from its binary wire format.
     *
     * The wire format is `[compact-u16 sig count][64-byte sigs...][message bytes]`.
     */
    public fun deserialize(bytes: ByteArray): VersionedTransaction {
      val source = Buffer().apply { write(bytes) }

      val numSignatures = ShortVecEncoding.decodeLength(source)
      val existingSignatures = Array(numSignatures) {
        source.readByteArray(TweetNaCl.Signature.SIGNATURE_BYTES.toLong())
      }
      val messageBytes = source.readByteArray()

      val message = VersionedMessage.deserialize(messageBytes)
      val signerKeys = message.staticAccountKeys
        .filter { it.isSigner }
        .map { it.publicKey }

      val signatures = signerKeys.zip(existingSignatures.toList()).toMap()

      return VersionedTransaction(
        message = message,
        signatures = signatures,
        rawSerializedMessage = messageBytes,
      )
    }
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

    /**
     * Builds the transaction, auto-detecting the message format:
     * - Legacy when no ALTs are provided
     * - V0 when ALTs are present
     */
    public fun build(): VersionedTransaction {
      if (lookupTableAccounts.isEmpty()) {
        return buildLegacy()
      }
      return buildV0()
    }

    /**
     * Builds the transaction with a legacy message format.
     *
     * @throws IllegalArgumentException if ALTs have been added
     */
    public fun buildLegacy(): VersionedTransaction {
      require(lookupTableAccounts.isEmpty()) {
        "Cannot build legacy transaction with address lookup tables. Use buildV0() or build()."
      }
      val message = Message.Builder()
        .apply {
          feePayer?.let { setFeePayer(it) }
          recentBlockHash?.let { setRecentBlockHash(it) }
          instructions.forEach { addInstruction(it) }
        }
        .build()
      return VersionedTransaction(
        message = VersionedMessage.Legacy(message),
      )
    }

    /**
     * Builds the transaction with a V0 message format, even if no ALTs
     * are provided (the address table lookups list will be empty).
     */
    public fun buildV0(): VersionedTransaction {
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
      )
    }
  }
}
