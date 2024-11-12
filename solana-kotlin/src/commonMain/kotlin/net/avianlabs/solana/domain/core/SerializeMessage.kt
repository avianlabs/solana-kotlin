package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.TweetNaCl
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import net.avianlabs.solana.tweetnacl.vendor.decodeBase58
import net.avianlabs.solana.vendor.ShortVecEncoding
import net.avianlabs.solana.vendor.ShortVecLength
import okio.Buffer

private const val RECENT_BLOCK_HASH_LENGTH = 32

private class Header private constructor(
  val numRequiredSignatures: Byte,
  val numReadonlySignedAccounts: Byte,
  val numReadonlyUnsignedAccounts: Byte,
) {

  constructor(accountKeys: List<AccountMeta>) : this(
    numRequiredSignatures = accountKeys.count { it.isSigner }.toByte(),
    numReadonlySignedAccounts = accountKeys.count { it.isSigner && !it.isWritable }.toByte(),
    numReadonlyUnsignedAccounts = accountKeys.count { !it.isSigner && !it.isWritable }.toByte(),
  )

  fun toByteArray(): ByteArray = byteArrayOf(
    numRequiredSignatures,
    numReadonlySignedAccounts,
    numReadonlyUnsignedAccounts
  )

  override fun toString(): String =
    "numRequiredSignatures: $numRequiredSignatures, " +
        "numReadOnlySignedAccounts: $numReadonlySignedAccounts, " +
        "numReadOnlyUnsignedAccounts: $numReadonlyUnsignedAccounts"

  companion object {
    const val HEADER_LENGTH = 3

    fun fromByteArray(bytes: ByteArray): Header = Header(
      numRequiredSignatures = bytes[0],
      numReadonlySignedAccounts = bytes[1],
      numReadonlyUnsignedAccounts = bytes[2],
    )
  }
}

private class CompiledInstruction(
  val programIdIndex: Byte,
  val keyIndicesLength: ShortVecLength,
  val keyIndices: ByteArray,
  val dataLength: ShortVecLength,
  val data: ByteArray,
) {

  val bytes: Int
    get() =
      1 + // programIdIndex is one byte
          keyIndicesLength.size + keyIndices.size + dataLength.size + data.size
}

private fun List<AccountMeta>.findAccountIndex(key: PublicKey): Int =
  indexOfFirst { it.publicKey == key }
    .takeIf { it != -1 } ?: error("Account $key not found")

private fun Message.compileAccountKeys(feePayer: PublicKey): List<AccountMeta> =
  // ensure fee payer is the first account
  listOf(AccountMeta(feePayer, isSigner = true, isWritable = true)) +
      (accountKeys.filter { it.publicKey != feePayer })

public fun Message.serialize(): ByteArray {
  requireNotNull(feePayer) { "feePayer required" }
  requireNotNull(recentBlockHash) { "recentBlockhash required" }
  require(instructions.isNotEmpty()) { "No instructions provided" }
  val keysList = compileAccountKeys(feePayer)
  val accountKeysSize = keysList.size
  val accountAddressesLength = ShortVecEncoding.encodeLength(accountKeysSize)
  val compiledInstructions = instructions.map { instruction ->
    val keysSize = instruction.keys.size
    val keyIndices = ByteArray(keysSize)
    for (i in 0 until keysSize) {
      keyIndices[i] = keysList.findAccountIndex(instruction.keys[i].publicKey).toByte()
    }
    CompiledInstruction(
      programIdIndex = keysList.findAccountIndex(instruction.programId).toByte(),
      keyIndicesLength = ShortVecEncoding.encodeLength(keysSize),
      keyIndices = keyIndices,
      dataLength = ShortVecEncoding.encodeLength(instruction.data.count()),
      data = instruction.data,
    )
  }
  val accountsKeyBufferSize = accountKeysSize * TweetNaCl.Signature.PUBLIC_KEY_BYTES
  val instructionsLength = ShortVecEncoding.encodeLength(compiledInstructions.size)
  val compiledInstructionsBytes = compiledInstructions.sumOf { it.bytes }
  val bufferSize =
    (Header.HEADER_LENGTH + RECENT_BLOCK_HASH_LENGTH + accountAddressesLength.size
        + accountsKeyBufferSize + instructionsLength.size
        + compiledInstructionsBytes)

  val accountKeysBytes = keysList.map { accountMeta ->
    accountMeta.publicKey.toByteArray()
  }.reduce { acc, bytes -> acc + bytes }

  val messageHeader = Header(keysList)

  val buffer = Buffer().apply {
    write(messageHeader.toByteArray())
    write(accountAddressesLength)
    write(accountKeysBytes)
    write(recentBlockHash.decodeBase58())
    write(instructionsLength)
    for (compiledInstruction in compiledInstructions) {
      writeByte(compiledInstruction.programIdIndex.toInt())
      write(compiledInstruction.keyIndicesLength)
      write(compiledInstruction.keyIndices)
      write(compiledInstruction.dataLength)
      write(compiledInstruction.data)
    }
  }
  return buffer.readByteArray(bufferSize.toLong())
}
