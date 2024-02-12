package net.avianlabs.solana.domain.core

import net.avianlabs.solana.domain.program.AssociatedTokenProgram
import net.avianlabs.solana.domain.program.SystemProgram
import net.avianlabs.solana.domain.program.TokenProgram
import net.avianlabs.solana.domain.program.TokenProgramBase
import net.avianlabs.solana.methods.TransactionResponse
import net.avianlabs.solana.vendor.decodeBase58
import net.avianlabs.solana.vendor.encodeToBase58String
import okio.Buffer

public data class DecodedTransaction(
  val instructions: List<DecodedInstruction>,
  val signatures: List<SignaturePublicKeyPair>,
)

public sealed class DecodedInstruction(
  public open val program: PublicKey,
) {
  public data class Raw(
    override val program: PublicKey,
    val accounts: List<AccountMeta>?,
    val data: ByteArray?,
  ) : DecodedInstruction(program) {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other == null || this::class != other::class) return false

      other as Raw

      if (program != other.program) return false
      if (accounts != other.accounts) return false
      if (data != null) {
        if (other.data == null) return false
        if (!data.contentEquals(other.data)) return false
      } else if (other.data != null) return false

      return true
    }

    override fun hashCode(): Int {
      var result = program.hashCode()
      result = 31 * result + (accounts?.hashCode() ?: 0)
      result = 31 * result + (data?.contentHashCode() ?: 0)
      return result
    }

  }

  public sealed class SystemProgram(
    public val programIndex: UInt,
  ) : DecodedInstruction(net.avianlabs.solana.domain.program.SystemProgram.programId) {

    public data class Transfer(
      val from: PublicKey,
      val to: PublicKey,
      val lamports: Long,
    ) : SystemProgram(net.avianlabs.solana.domain.program.SystemProgram.Instruction.Transfer.index)
  }

  public sealed class TokenProgram(
    public val programIndex: UByte,
  ) : DecodedInstruction(net.avianlabs.solana.domain.program.TokenProgram.programId) {

    public data class Transfer(
      val source: PublicKey,
      val destination: PublicKey,
      val owner: PublicKey,
      val amount: Long,
    ) :
      TokenProgram(net.avianlabs.solana.domain.program.TokenProgramBase.Instruction.Transfer.index)

    public data class TransferChecked(
      val source: PublicKey,
      val mint: PublicKey,
      val destination: PublicKey,
      val owner: PublicKey,
      val amount: Long,
      val decimals: UByte,
    ) :
      TokenProgram(net.avianlabs.solana.domain.program.TokenProgramBase.Instruction.TransferChecked.index)
  }

  public sealed class AssociatedTokenProgram :
    DecodedInstruction(net.avianlabs.solana.domain.program.AssociatedTokenProgram.programId) {
    public data class CreatedAssociatedAccount(
      val payer: PublicKey,
      val associatedAccount: PublicKey,
      val owner: PublicKey,
      val mint: PublicKey,
      val programId: PublicKey,
    ) : AssociatedTokenProgram()
  }
}

public data class SignaturePublicKeyPair(
  val signature: ByteArray?,
  val publicKey: PublicKey,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as SignaturePublicKeyPair

    if (signature != null) {
      if (other.signature == null) return false
      if (!signature.contentEquals(other.signature)) return false
    } else if (other.signature != null) return false
    return publicKey == other.publicKey
  }

  override fun hashCode(): Int {
    var result = signature?.contentHashCode() ?: 0
    result = 31 * result + publicKey.hashCode()
    return result
  }

  override fun toString(): String {
    return "SignaturePublicKeyPair(signature=${signature?.encodeToBase58String()}, publicKey=$publicKey)"
  }
}

private val defaultSignature = ByteArray(64) { 0 }.encodeToBase58String()

private fun TransactionResponse.Message.isAccountWritable(index: Int): Boolean =
  index < header.numRequiredSignatures - header.numReadonlySignedAccounts
    || (index >= header.numRequiredSignatures && index < accountKeys.size - header.numReadonlyUnsignedAccounts)

private fun TransactionResponse.Message.isAccountSigner(index: Int): Boolean =
  index < header.numRequiredSignatures

public fun TransactionResponse.decode(): DecodedTransaction? {
  val message = transaction?.message ?: return null
  val accounts = transaction.message.accountKeys.map { PublicKey.fromBase58(it) }
  val signatures = transaction.signatures.mapIndexed { index, signature ->
    SignaturePublicKeyPair(
      signature = if (signature == defaultSignature) null else signature.decodeBase58(),
      publicKey = accounts[index],
    )
  }
  val instructions: List<DecodedInstruction> = message.instructions.map { instruction ->
    val programKey = accounts[instruction.programIdIndex.toInt()]
    val accountsMeta = instruction.accounts?.map {
      val index = it.toInt()
      val publicKey = accounts[index]
      AccountMeta(
        publicKey = publicKey,
        isSigner = signatures.find { it.publicKey == publicKey } != null
          || message.isAccountSigner(index),
        isWritable = message.isAccountWritable(index),
      )
    }
    val data = instruction.data!!.decodeBase58()
    val buffer = Buffer().write(data)
    val raw = DecodedInstruction.Raw(
      program = programKey,
      accounts = accountsMeta,
      data = data,
    )
    when (programKey) {
      SystemProgram.programId -> {
        val programIndex = buffer.readInt().toUInt()
        when (programIndex) {
          SystemProgram.Instruction.Transfer.index -> DecodedInstruction.SystemProgram.Transfer(
            from = accountsMeta!![0].publicKey,
            to = accountsMeta[1].publicKey,
            lamports = buffer.readLongLe(),
          )

          else -> raw
        }
      }

      TokenProgram.programId -> {
        val programIndex = buffer.readByte().toUByte()
        when (programIndex) {
          TokenProgramBase.Instruction.Transfer.index -> {
            val (source, destination, owner) = accountsMeta!!
            DecodedInstruction.TokenProgram.Transfer(
              source = source.publicKey,
              destination = destination.publicKey,
              owner = owner.publicKey,
              amount = buffer.readLongLe(),
            )
          }

          TokenProgramBase.Instruction.TransferChecked.index -> {
            val (source, mint, destination, owner) = accountsMeta!!
            DecodedInstruction.TokenProgram.TransferChecked(
              source = source.publicKey,
              destination = destination.publicKey,
              mint = mint.publicKey,
              owner = owner.publicKey,
              amount = buffer.readLongLe(),
              decimals = buffer.readByte().toUByte(),
            )
          }

          else -> {
            raw
          }
        }
      }

      AssociatedTokenProgram.programId -> {
        val (
          payer,
          associatedAccount,
          owner,
          mint,
        ) = accountsMeta!!
        val programId = accountsMeta[5]
        DecodedInstruction.AssociatedTokenProgram.CreatedAssociatedAccount(
          payer = payer.publicKey,
          associatedAccount = associatedAccount.publicKey,
          owner = owner.publicKey,
          mint = mint.publicKey,
          programId = programId.publicKey,
        )
      }

      else -> raw
    }
  }
  return DecodedTransaction(
    instructions = instructions,
    signatures = signatures,
  )
}
