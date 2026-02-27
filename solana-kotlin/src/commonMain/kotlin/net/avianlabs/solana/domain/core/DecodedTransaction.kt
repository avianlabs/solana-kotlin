package net.avianlabs.solana.domain.core

import net.avianlabs.solana.domain.program.AssociatedTokenProgram
import net.avianlabs.solana.domain.program.SystemProgram
import net.avianlabs.solana.domain.program.TokenProgram
import net.avianlabs.solana.methods.TransactionResponse
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import net.avianlabs.solana.tweetnacl.vendor.decodeBase58
import net.avianlabs.solana.tweetnacl.vendor.encodeToBase58String
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

    public data class CreateAccount(
      val from: PublicKey,
      val newAccount: PublicKey,
      val lamports: Long,
      val space: Long,
      val owner: PublicKey,
    ) : SystemProgram(
      net.avianlabs.solana.domain.program.SystemProgram.Instruction.CreateAccount.index
    )

    public data class InitializeNonceAccount(
      val nonceAccount: PublicKey,
      val authorizedPubkey: PublicKey,
    ) : SystemProgram(
      net.avianlabs.solana.domain.program.SystemProgram.Instruction.InitializeNonceAccount.index
    )

    public data class AdvanceNonceAccount(
      val nonceAccount: PublicKey,
      val authorizedPubkey: PublicKey,
    ) : SystemProgram(
      net.avianlabs.solana.domain.program.SystemProgram.Instruction.AdvanceNonceAccount.index
    )

    public data class WithdrawNonceAccount(
      val nonceAccount: PublicKey,
      val authorizedPubkey: PublicKey,
      val destination: PublicKey,
      val lamports: Long,
    ) : SystemProgram(
      net.avianlabs.solana.domain.program.SystemProgram.Instruction.WithdrawNonceAccount.index
    )

    public data class AuthorizeNonceAccount(
      val nonceAccount: PublicKey,
      val authorizedPubkey: PublicKey,
      val newAuthorizedPubkey: PublicKey,
    ) : SystemProgram(
      net.avianlabs.solana.domain.program.SystemProgram.Instruction.AuthorizeNonceAccount.index
    )
  }

  public sealed class TokenProgram(
    public val programIndex: UByte,
  ) : DecodedInstruction(net.avianlabs.solana.domain.program.TokenProgram.programId) {

    public data class Transfer(
      val source: PublicKey,
      val destination: PublicKey,
      val owner: PublicKey,
      val amount: Long,
    ) : TokenProgram(
      net.avianlabs.solana.domain.program.TokenProgram.Instruction.Transfer.index
    )

    public data class TransferChecked(
      val source: PublicKey,
      val mint: PublicKey,
      val destination: PublicKey,
      val owner: PublicKey,
      val amount: Long,
      val decimals: UByte,
    ) : TokenProgram(
      net.avianlabs.solana.domain.program.TokenProgram.Instruction.TransferChecked.index
    )
  }

  public sealed class AssociatedTokenProgram : DecodedInstruction(
    net.avianlabs.solana.domain.program.AssociatedTokenProgram.programId
  ) {
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
  // For V0 transactions, the full account list includes addresses loaded from ALTs.
  // Order: [static keys] + [writable from loadedAddresses] + [readonly from loadedAddresses]
  val staticAccounts = transaction.message.accountKeys.map { PublicKey.fromBase58(it) }
  val loadedWritable = meta?.loadedAddresses?.writable?.map { PublicKey.fromBase58(it) }.orEmpty()
  val loadedReadonly = meta?.loadedAddresses?.readonly?.map { PublicKey.fromBase58(it) }.orEmpty()
  val accounts = staticAccounts + loadedWritable + loadedReadonly
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
          SystemProgram.Instruction.Transfer.index -> {
            val (from, to) = accountsMeta!!
            DecodedInstruction.SystemProgram.Transfer(
              from = from.publicKey,
              to = to.publicKey,
              lamports = buffer.readLongLe(),
            )
          }

          SystemProgram.Instruction.AdvanceNonceAccount.index -> {
            val (nonceAccount, authorizedPubkey) = accountsMeta!!
            DecodedInstruction.SystemProgram.AdvanceNonceAccount(
              nonceAccount = nonceAccount.publicKey,
              authorizedPubkey = authorizedPubkey.publicKey,
            )
          }

          SystemProgram.Instruction.AuthorizeNonceAccount.index -> {
            val (nonceAccount, authorizedPubkey, newAuthorizedPubkey) = accountsMeta!!
            DecodedInstruction.SystemProgram.AuthorizeNonceAccount(
              nonceAccount = nonceAccount.publicKey,
              authorizedPubkey = authorizedPubkey.publicKey,
              newAuthorizedPubkey = newAuthorizedPubkey.publicKey,
            )
          }

          SystemProgram.Instruction.CreateAccount.index -> {
            val (from, newAccount, owner) = accountsMeta!!
            DecodedInstruction.SystemProgram.CreateAccount(
              from = from.publicKey,
              newAccount = newAccount.publicKey,
              lamports = buffer.readLongLe(),
              space = buffer.readLongLe(),
              owner = owner.publicKey,
            )
          }

          SystemProgram.Instruction.InitializeNonceAccount.index -> {
            val (nonceAccount, authorizedPubkey) = accountsMeta!!
            DecodedInstruction.SystemProgram.InitializeNonceAccount(
              nonceAccount = nonceAccount.publicKey,
              authorizedPubkey = authorizedPubkey.publicKey,
            )
          }

          SystemProgram.Instruction.WithdrawNonceAccount.index -> {
            val (nonceAccount, authorizedPubkey, destination) = accountsMeta!!
            DecodedInstruction.SystemProgram.WithdrawNonceAccount(
              nonceAccount = nonceAccount.publicKey,
              authorizedPubkey = authorizedPubkey.publicKey,
              destination = destination.publicKey,
              lamports = buffer.readLongLe(),
            )
          }

          else -> raw
        }
      }

      TokenProgram.programId -> {
        val programIndex = buffer.readByte().toUByte()
        when (programIndex) {
          TokenProgram.Instruction.Transfer.index -> {
            val (source, destination, owner) = accountsMeta!!
            DecodedInstruction.TokenProgram.Transfer(
              source = source.publicKey,
              destination = destination.publicKey,
              owner = owner.publicKey,
              amount = buffer.readLongLe(),
            )
          }

          TokenProgram.Instruction.TransferChecked.index -> {
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
          programId,
        ) = accountsMeta!!
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
