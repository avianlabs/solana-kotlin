package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import net.avianlabs.solana.tweetnacl.vendor.decodeBase58
import net.avianlabs.solana.vendor.ShortVecEncoding
import okio.Buffer

/**
 * Serializes a [VersionedMessage] to its binary wire format.
 *
 * - [VersionedMessage.Legacy] delegates to [Message.serialize]
 * - [VersionedMessage.V0] produces `[0x80][header][accounts][blockhash][instructions][ALT lookups]`
 */
public fun VersionedMessage.serialize(): ByteArray = when (this) {
  is VersionedMessage.Legacy -> message.serialize()
  is VersionedMessage.V0 -> serializeV0()
}

private fun VersionedMessage.V0.serializeV0(): ByteArray {
  val feePayer = requireNotNull(feePayer) { "feePayer required" }
  requireNotNull(recentBlockHash) { "recentBlockhash required" }
  require(instructions.isNotEmpty()) { "No instructions provided" }

  val keysList = compileStaticAccountKeys(feePayer)

  // Build the full account list for instruction index computation:
  // [static keys] + [writable from ALTs] + [readonly from ALTs]
  val fullAccountKeys = buildFullAccountKeys(keysList)

  val accountKeysSize = keysList.size
  val accountAddressesLength = ShortVecEncoding.encodeLength(accountKeysSize)

  val compiledInstructions = instructions.map { instruction ->
    val keysSize = instruction.keys.size
    val keyIndices = ByteArray(keysSize)
    for (i in 0 until keysSize) {
      keyIndices[i] = fullAccountKeys.findAccountIndex(instruction.keys[i].publicKey).toByte()
    }
    CompiledInstruction(
      programIdIndex = fullAccountKeys.findAccountIndex(instruction.programId).toByte(),
      keyIndicesLength = ShortVecEncoding.encodeLength(keysSize),
      keyIndices = keyIndices,
      dataLength = ShortVecEncoding.encodeLength(instruction.data.count()),
      data = instruction.data,
    )
  }

  val instructionsLength = ShortVecEncoding.encodeLength(compiledInstructions.size)

  val messageHeader = Header(keysList)

  val buffer = Buffer().apply {
    // V0 version prefix
    writeByte(0x80)
    // Header (3 bytes)
    write(messageHeader.toByteArray())
    // Static account keys
    write(accountAddressesLength)
    keysList.forEach { write(it.publicKey.toByteArray()) }
    // Recent blockhash
    write(recentBlockHash.decodeBase58())
    // Instructions
    write(instructionsLength)
    for (compiledInstruction in compiledInstructions) {
      writeByte(compiledInstruction.programIdIndex.toInt())
      write(compiledInstruction.keyIndicesLength)
      write(compiledInstruction.keyIndices)
      write(compiledInstruction.dataLength)
      write(compiledInstruction.data)
    }
    // Address table lookups
    write(ShortVecEncoding.encodeLength(addressTableLookups.size))
    for (lookup in addressTableLookups) {
      write(lookup.accountKey.toByteArray())
      write(ShortVecEncoding.encodeLength(lookup.writableIndexes.size))
      for (index in lookup.writableIndexes) {
        writeByte(index.toInt())
      }
      write(ShortVecEncoding.encodeLength(lookup.readonlyIndexes.size))
      for (index in lookup.readonlyIndexes) {
        writeByte(index.toInt())
      }
    }
  }
  return buffer.readByteArray()
}

/**
 * Compiles static account keys with fee payer first, matching legacy behavior.
 */
private fun VersionedMessage.V0.compileStaticAccountKeys(
  feePayer: PublicKey,
): List<AccountMeta> =
  listOf(AccountMeta(feePayer, isSigner = true, isWritable = true)) +
    (staticAccountKeys.filter { it.publicKey != feePayer })

/**
 * Builds the full logical account list used for instruction index computation:
 * [static keys] + [writable accounts from ALTs] + [readonly accounts from ALTs]
 *
 * Uses [resolvedLookupTables] to map ALT indices to actual public keys.
 * ALT-loaded accounts are always non-signers.
 */
private fun VersionedMessage.V0.buildFullAccountKeys(
  staticKeys: List<AccountMeta>,
): List<AccountMeta> {
  val altMap = resolvedLookupTables.associateBy { it.key }
  val result = staticKeys.toMutableList()
  // Writable ALT accounts first (all ALTs, writable indices)
  for (lookup in addressTableLookups) {
    val alt = altMap[lookup.accountKey]
      ?: error("Resolved ALT not found for ${lookup.accountKey}")
    for (index in lookup.writableIndexes) {
      result.add(
        AccountMeta(alt.addresses[index.toInt()], isSigner = false, isWritable = true)
      )
    }
  }
  // Readonly ALT accounts (all ALTs, readonly indices)
  for (lookup in addressTableLookups) {
    val alt = altMap[lookup.accountKey]
      ?: error("Resolved ALT not found for ${lookup.accountKey}")
    for (index in lookup.readonlyIndexes) {
      result.add(
        AccountMeta(alt.addresses[index.toInt()], isSigner = false, isWritable = false)
      )
    }
  }
  return result
}
