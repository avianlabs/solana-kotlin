package net.avianlabs.solana.domain.program

import net.avianlabs.solana.domain.core.AccountMeta
import net.avianlabs.solana.domain.core.PublicKey
import net.avianlabs.solana.domain.core.TransactionInstruction
import okio.Buffer

private val TOKEN_PROGRAM_ID = PublicKey.fromBase58("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA")

public object TokenProgram : Program(
  programId = TOKEN_PROGRAM_ID
) {

  public enum class Instruction(
    public val index: UByte,
  ) {
    InitializeMint(0u),
    InitializeAccount(1u),
    InitializeMultisig(2u),
    Transfer(3u),
    Approve(4u),
    Revoke(5u),
    SetAuthority(6u),
    MintTo(7u),
    Burn(8u),
    CloseAccount(9u),
    FreezeAccount(10u),
    ThawAccount(11u),
    TransferChecked(12u),
    ApproveChecked(13u),
    MintToChecked(14u),
    BurnChecked(15u),
    InitializeAccount2(16u),
    SyncNative(17u),
    InitializeAccount3(18u),
    ;
  }

  public fun transfer(
    source: PublicKey,
    destination: PublicKey,
    owner: PublicKey,
    amount: ULong,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(source, isSigner = false, isWritable = true),
      AccountMeta(destination, isSigner = false, isWritable = true),
      AccountMeta(owner, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.Transfer.index.toInt())
      .writeLongLe(amount.toLong())
      .readByteArray(),
  )

  public fun closeAccount(
    account: PublicKey,
    destination: PublicKey,
    owner: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
      AccountMeta(destination, isSigner = false, isWritable = true),
      AccountMeta(owner, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.CloseAccount.index.toInt())
      .readByteArray(),
  )
}
