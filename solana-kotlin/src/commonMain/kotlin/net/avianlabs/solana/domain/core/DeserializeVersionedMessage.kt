package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import net.avianlabs.solana.tweetnacl.vendor.encodeToBase58String
import net.avianlabs.solana.vendor.ShortVecEncoding
import okio.Buffer

/**
 * Deserializes a [VersionedMessage] from its binary wire format.
 *
 * - Legacy messages: `[header][accounts][blockhash][instructions]`
 * - V0 messages: `[0x80][header][accounts][blockhash][instructions][ALT lookups]`
 *
 * V0 messages are returned with `resolvedLookupTables = emptyList()` because the
 * actual lookup table contents are not available from the wire bytes alone. Compiled
 * instructions that reference ALT-loaded accounts (indices beyond static keys) use
 * placeholder [AccountMeta] entries.
 */
public fun VersionedMessage.Companion.deserialize(bytes: ByteArray): VersionedMessage {
  val source = Buffer().apply { write(bytes) }
  val firstByte = bytes[0].toInt() and 0xFF
  return if (firstByte and 0x80 != 0) {
    val version = firstByte and 0x7F
    require(version == 0) { "Unsupported message version: $version" }
    source.readByte() // consume version prefix
    deserializeV0(source)
  } else {
    deserializeLegacy(source)
  }
}

private fun deserializeLegacy(source: Buffer): VersionedMessage.Legacy {
  val header = readHeader(source)
  val accountKeys = readAccountKeys(source, header)
  val recentBlockHash = readBlockHash(source)
  val instructions = readInstructions(source, accountKeys)

  val feePayer = accountKeys.firstOrNull()?.publicKey
  return VersionedMessage.Legacy(
    Message(
      feePayer = feePayer,
      recentBlockHash = recentBlockHash,
      accountKeys = accountKeys,
      instructions = instructions,
    )
  )
}

private fun deserializeV0(source: Buffer): VersionedMessage.V0 {
  val header = readHeader(source)
  val accountKeys = readAccountKeys(source, header)
  val recentBlockHash = readBlockHash(source)
  val instructions = readInstructions(source, accountKeys)
  val addressTableLookups = readAddressTableLookups(source)

  val feePayer = accountKeys.firstOrNull()?.publicKey
  return VersionedMessage.V0(
    feePayer = feePayer,
    recentBlockHash = recentBlockHash,
    staticAccountKeys = accountKeys,
    instructions = instructions,
    addressTableLookups = addressTableLookups,
  )
}

private fun readHeader(source: Buffer): Header {
  val headerBytes = source.readByteArray(Header.HEADER_LENGTH.toLong())
  return Header.fromByteArray(headerBytes)
}

private fun readAccountKeys(source: Buffer, header: Header): List<AccountMeta> {
  val numAccounts = ShortVecEncoding.decodeLength(source)
  val numRequiredSignatures = header.numRequiredSignatures.toInt() and 0xFF
  val numReadonlySignedAccounts = header.numReadonlySignedAccounts.toInt() and 0xFF
  val numReadonlyUnsignedAccounts = header.numReadonlyUnsignedAccounts.toInt() and 0xFF

  return List(numAccounts) { index ->
    val key = PublicKey(source.readByteArray(TweetNaCl.Signature.PUBLIC_KEY_BYTES.toLong()))
    val isSigner = index < numRequiredSignatures
    val isWritable = if (isSigner) {
      index < numRequiredSignatures - numReadonlySignedAccounts
    } else {
      index < numAccounts - numReadonlyUnsignedAccounts
    }
    AccountMeta(key, isSigner = isSigner, isWritable = isWritable)
  }
}

private fun readBlockHash(source: Buffer): String =
  source.readByteArray(RECENT_BLOCK_HASH_LENGTH.toLong()).encodeToBase58String()

private fun readInstructions(
  source: Buffer,
  accountKeys: List<AccountMeta>,
): List<TransactionInstruction> {
  val numInstructions = ShortVecEncoding.decodeLength(source)
  return List(numInstructions) {
    val programIdIndex = source.readByte().toInt() and 0xFF
    val numKeyIndices = ShortVecEncoding.decodeLength(source)
    val keyIndices = source.readByteArray(numKeyIndices.toLong())
    val dataLength = ShortVecEncoding.decodeLength(source)
    val data = source.readByteArray(dataLength.toLong())

    val programId = accountKeys.getOrPlaceholder(programIdIndex).publicKey
    val keys = keyIndices.map { index ->
      accountKeys.getOrPlaceholder(index.toInt() and 0xFF)
    }

    TransactionInstruction(
      programId = programId,
      keys = keys,
      data = data,
    )
  }
}

private fun readAddressTableLookups(source: Buffer): List<MessageAddressTableLookup> {
  val numLookups = ShortVecEncoding.decodeLength(source)
  return List(numLookups) {
    val accountKey = PublicKey(source.readByteArray(TweetNaCl.Signature.PUBLIC_KEY_BYTES.toLong()))
    val numWritable = ShortVecEncoding.decodeLength(source)
    val writableIndexes = List(numWritable) { source.readByte().toUByte() }
    val numReadonly = ShortVecEncoding.decodeLength(source)
    val readonlyIndexes = List(numReadonly) { source.readByte().toUByte() }
    MessageAddressTableLookup(
      accountKey = accountKey,
      writableIndexes = writableIndexes,
      readonlyIndexes = readonlyIndexes,
    )
  }
}

/**
 * Returns the [AccountMeta] at [index], or a placeholder for out-of-range indices.
 *
 * Out-of-range indices occur in V0 messages when compiled instructions reference
 * ALT-loaded accounts that are not part of the static key list.
 */
private fun List<AccountMeta>.getOrPlaceholder(index: Int): AccountMeta =
  getOrElse(index) {
    AccountMeta(PublicKey(ByteArray(32)), isSigner = false, isWritable = false)
  }
