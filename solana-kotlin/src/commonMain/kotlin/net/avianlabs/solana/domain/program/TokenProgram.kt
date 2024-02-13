package net.avianlabs.solana.domain.program

import net.avianlabs.solana.domain.core.AccountMeta
import net.avianlabs.solana.domain.core.PublicKey
import net.avianlabs.solana.domain.core.TransactionInstruction
import net.avianlabs.solana.domain.program.Program.Companion.createTransactionInstruction
import okio.Buffer


public interface TokenProgram : Program {

  public enum class Instruction(
    public val index: UByte,
  ) {
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
    InitializeMultisig2(19u),
    InitializeMint2(20u),
    GetAccountDataSize(21u),
    InitializeImmutableOwner(22u),
    AmountToUiAmount(23u),
    UiAmountToAmount(24u),
    InitializeMintCloseAuthority(25u),
    ;
  }

  public fun transfer(
    source: PublicKey,
    destination: PublicKey,
    owner: PublicKey,
    amount: ULong,
  ): TransactionInstruction

  public fun closeAccount(
    account: PublicKey,
    destination: PublicKey,
    owner: PublicKey,
  ): TransactionInstruction

  public fun transferChecked(
    source: PublicKey,
    mint: PublicKey,
    destination: PublicKey,
    owner: PublicKey,
    amount: ULong,
    decimals: UByte,
  ): TransactionInstruction

  public companion object : TokenProgram {
    public override val programId: PublicKey =
      PublicKey.fromBase58("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA")

    public override fun transfer(
      source: PublicKey,
      destination: PublicKey,
      owner: PublicKey,
      amount: ULong,
    ): TransactionInstruction = createTransferInstruction(
      source = source,
      destination = destination,
      owner = owner,
      amount = amount,
      programId = programId,
    )

    internal fun createTransferInstruction(
      source: PublicKey,
      destination: PublicKey,
      owner: PublicKey,
      amount: ULong,
      programId: PublicKey
    ) = createTransactionInstruction(
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

    public override fun closeAccount(
      account: PublicKey,
      destination: PublicKey,
      owner: PublicKey,
    ): TransactionInstruction = createCloseAccountInstruction(
      account = account,
      destination = destination,
      owner = owner,
      programId = programId,
    )

    internal fun createCloseAccountInstruction(
      account: PublicKey,
      destination: PublicKey,
      owner: PublicKey,
      programId: PublicKey
    ) = createTransactionInstruction(
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

    public override fun transferChecked(
      source: PublicKey,
      mint: PublicKey,
      destination: PublicKey,
      owner: PublicKey,
      amount: ULong,
      decimals: UByte,
    ): TransactionInstruction = createTransferCheckedInstruction(
      source = source,
      mint = mint,
      destination = destination,
      owner = owner,
      amount = amount,
      decimals = decimals,
      programId = programId,
    )

    internal fun createTransferCheckedInstruction(
      source: PublicKey,
      mint: PublicKey,
      destination: PublicKey,
      owner: PublicKey,
      amount: ULong,
      decimals: UByte,
      programId: PublicKey
    ) = createTransactionInstruction(
      programId = programId,
      keys = listOf(
        AccountMeta(source, isSigner = false, isWritable = true),
        AccountMeta(mint, isSigner = false, isWritable = false),
        AccountMeta(destination, isSigner = false, isWritable = true),
        AccountMeta(owner, isSigner = true, isWritable = false),
      ),
      data = Buffer()
        .writeByte(Instruction.TransferChecked.index.toInt())
        .writeLongLe(amount.toLong())
        .writeByte(decimals.toInt())
        .readByteArray(),
    )
  }
}
