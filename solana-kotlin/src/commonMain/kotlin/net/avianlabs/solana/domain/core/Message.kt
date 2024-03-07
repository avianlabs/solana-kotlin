package net.avianlabs.solana.domain.core

import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import net.avianlabs.solana.vendor.ShortvecEncoding
import net.avianlabs.solana.tweetnacl.vendor.decodeBase58
import okio.Buffer

public class Message(
  public var feePayer: PublicKey? = null,
  public var recentBlockHash: String? = null,
  accountKeys: AccountKeysList = AccountKeysList(),
  instructions: List<TransactionInstruction> = emptyList(),
) {

  private val _accountKeys: AccountKeysList = accountKeys
  private val _instructions: MutableList<TransactionInstruction> = instructions.toMutableList()


  public val accountKeys: List<AccountMeta>
    get() = _accountKeys.list

  public val instructions: List<TransactionInstruction>
    get() = _instructions

  private class MessageHeader {
    var numRequiredSignatures: Byte = 0
    var numReadonlySignedAccounts: Byte = 0
    var numReadonlyUnsignedAccounts: Byte = 0
    fun toByteArray(): ByteArray {
      return byteArrayOf(
        numRequiredSignatures,
        numReadonlySignedAccounts,
        numReadonlyUnsignedAccounts
      )
    }

    override fun toString(): String {
      return "numRequiredSignatures: $numRequiredSignatures, numReadOnlySignedAccounts: $numReadonlySignedAccounts, numReadOnlyUnsignedAccounts: $numReadonlyUnsignedAccounts"
    }

    companion object {
      const val HEADER_LENGTH = 3

      fun fromByteArray(bytes: ByteArray): MessageHeader {
        val header = MessageHeader()
        header.numRequiredSignatures = bytes[0]
        header.numReadonlySignedAccounts = bytes[1]
        header.numReadonlyUnsignedAccounts = bytes[2]
        return header
      }
    }
  }

  private class CompiledInstruction {
    var programIdIndex: Byte = 0
    lateinit var keyIndicesCount: ByteArray
    lateinit var keyIndices: ByteArray
    lateinit var dataLength: ByteArray
    lateinit var data: ByteArray

    // 1 = programIdIndex length
    val length: Int
      get() =// 1 = programIdIndex length
        1 + keyIndicesCount.size + keyIndices.size + dataLength.size + data.size
  }

  public fun addInstruction(instruction: TransactionInstruction): Message {
    _accountKeys.addAll(instruction.keys)
    _accountKeys.add(AccountMeta(instruction.programId, false, false))
    _instructions.add(instruction)
    return this
  }

  public fun serialize(): ByteArray {
    requireNotNull(recentBlockHash) { "recentBlockhash required" }
    require(_instructions.size != 0) { "No instructions provided" }
    val messageHeader = MessageHeader()
    val keysList = compileAccountKeys()
    val accountKeysSize = keysList.size
    val accountAddressesLength = ShortvecEncoding.encodeLength(accountKeysSize)
    var compiledInstructionsLength = 0
    val compiledInstructions: MutableList<CompiledInstruction> = ArrayList()
    for (instruction in _instructions) {
      val keysSize = instruction.keys.size
      val keyIndices = ByteArray(keysSize)
      for (i in 0 until keysSize) {
        keyIndices[i] = findAccountIndex(keysList, instruction.keys[i].publicKey).toByte()
      }
      val compiledInstruction = CompiledInstruction()
      compiledInstruction.programIdIndex =
        findAccountIndex(keysList, instruction.programId).toByte()
      compiledInstruction.keyIndicesCount = ShortvecEncoding.encodeLength(keysSize)
      compiledInstruction.keyIndices = keyIndices
      compiledInstruction.dataLength = ShortvecEncoding.encodeLength(instruction.data.count())
      compiledInstruction.data = instruction.data
      compiledInstructions.add(compiledInstruction)
      compiledInstructionsLength += compiledInstruction.length
    }
    val instructionsLength = ShortvecEncoding.encodeLength(compiledInstructions.size)
    val accountsKeyBufferSize = accountKeysSize * PublicKey.PUBLIC_KEY_LENGTH
    val bufferSize =
      (MessageHeader.HEADER_LENGTH + RECENT_BLOCK_HASH_LENGTH + accountAddressesLength.size
        + accountsKeyBufferSize + instructionsLength.size
        + compiledInstructionsLength)
    val out = Buffer()
    val accountKeysBuff = Buffer()
    for (accountMeta in keysList) {
      accountKeysBuff.write(accountMeta.publicKey.toByteArray())
      if (accountMeta.isSigner) {
        messageHeader.numRequiredSignatures =
          (messageHeader.numRequiredSignatures.plus(1)).toByte()
        if (!accountMeta.isWritable) {
          messageHeader.numReadonlySignedAccounts =
            (messageHeader.numReadonlySignedAccounts.plus(1)).toByte()
        }
      } else {
        if (!accountMeta.isWritable) {
          messageHeader.numReadonlyUnsignedAccounts =
            (messageHeader.numReadonlyUnsignedAccounts.plus(1)).toByte()
        }
      }
    }
    out.write(messageHeader.toByteArray())
    out.write(accountAddressesLength)
    out.write(accountKeysBuff, accountsKeyBufferSize.toLong())
    out.write(recentBlockHash!!.decodeBase58())
    out.write(instructionsLength)
    for (compiledInstruction in compiledInstructions) {
      out.writeByte(compiledInstruction.programIdIndex.toInt())
      out.write(compiledInstruction.keyIndicesCount)
      out.write(compiledInstruction.keyIndices)
      out.write(compiledInstruction.dataLength)
      out.write(compiledInstruction.data)
    }
    return out.readByteArray(bufferSize.toLong())
  }

  private fun compileAccountKeys(): List<AccountMeta> {
    val keysList: MutableList<AccountMeta> = _accountKeys.list
    val newList: MutableList<AccountMeta> = ArrayList()
    try {
      val feePayerIndex = findAccountIndex(keysList, feePayer!!)
      val feePayerMeta = keysList[feePayerIndex]
      newList.add(AccountMeta(feePayerMeta.publicKey, true, true))
      keysList.removeAt(feePayerIndex)
    } catch (e: RuntimeException) { // Fee payer not yet in list
      newList.add(AccountMeta(feePayer!!, true, true))
    }
    newList.addAll(keysList)
    return newList
  }

  private fun findAccountIndex(accountMetaList: List<AccountMeta>, key: PublicKey): Int {
    for (i in accountMetaList.indices) {
      if (accountMetaList[i].publicKey.equals(key)) {
        return i
      }
    }
    throw RuntimeException("unable to find account index")
  }

  override fun toString(): String =
    """Message(
          |  header: not set,
          |  accountKeys: [${_accountKeys.list.joinToString()}],
          |  recentBlockhash: $recentBlockHash,
          |  instructions: [${_instructions.joinToString()}]
      |)""".trimMargin()

}

private const val RECENT_BLOCK_HASH_LENGTH = 32
