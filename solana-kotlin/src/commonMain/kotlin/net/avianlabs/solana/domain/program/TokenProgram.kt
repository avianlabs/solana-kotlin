package net.avianlabs.solana.domain.program

import kotlin.Any
import kotlin.Long
import kotlin.String
import kotlin.UByte
import kotlin.ULong
import net.avianlabs.solana.domain.core.AccountMeta
import net.avianlabs.solana.domain.core.TransactionInstruction
import net.avianlabs.solana.domain.program.Program.Companion.createTransactionInstruction
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import okio.Buffer

public object TokenProgram : Program {
  public override val programId: PublicKey =
      PublicKey.fromBase58("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA")

  public val RENT: PublicKey = PublicKey.fromBase58("SysvarRent111111111111111111111111111111111")

  public val MINT_LENGTH: Long = 82L

  public val TOKEN_LENGTH: Long = 165L

  public val MULTISIG_LENGTH: Long = 355L

  public fun initializeMint(
    mint: PublicKey,
    decimals: UByte,
    mintAuthority: PublicKey,
    freezeAuthority: Any,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(mint, isSigner = false, isWritable = true),
      AccountMeta(RENT, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.InitializeMint.index.toInt())
      .writeByte(decimals.toInt())
      .write(mintAuthority.bytes)
    .readByteArray(),
  )

  internal fun createInitializeMintInstruction(
    mint: PublicKey,
    decimals: UByte,
    mintAuthority: PublicKey,
    freezeAuthority: Any,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(mint, isSigner = false, isWritable = true),
      AccountMeta(RENT, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.InitializeMint.index.toInt())
      .writeByte(decimals.toInt())
      .write(mintAuthority.bytes)
    .readByteArray(),
  )

  public fun initializeAccount(
    account: PublicKey,
    mint: PublicKey,
    owner: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(owner, isSigner = false, isWritable = false),
      AccountMeta(RENT, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.InitializeAccount.index.toInt())
    .readByteArray(),
  )

  internal fun createInitializeAccountInstruction(
    account: PublicKey,
    mint: PublicKey,
    owner: PublicKey,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(owner, isSigner = false, isWritable = false),
      AccountMeta(RENT, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.InitializeAccount.index.toInt())
    .readByteArray(),
  )

  public fun initializeMultisig(multisig: PublicKey, m: UByte): TransactionInstruction =
      createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(multisig, isSigner = false, isWritable = true),
      AccountMeta(RENT, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.InitializeMultisig.index.toInt())
      .writeByte(m.toInt())
    .readByteArray(),
  )

  internal fun createInitializeMultisigInstruction(
    multisig: PublicKey,
    m: UByte,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(multisig, isSigner = false, isWritable = true),
      AccountMeta(RENT, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.InitializeMultisig.index.toInt())
      .writeByte(m.toInt())
    .readByteArray(),
  )

  public fun transfer(
    source: PublicKey,
    destination: PublicKey,
    authority: PublicKey,
    amount: ULong,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(source, isSigner = false, isWritable = true),
      AccountMeta(destination, isSigner = false, isWritable = true),
      AccountMeta(authority, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.Transfer.index.toInt())
      .writeLongLe(amount.toLong())
    .readByteArray(),
  )

  internal fun createTransferInstruction(
    source: PublicKey,
    destination: PublicKey,
    authority: PublicKey,
    amount: ULong,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(source, isSigner = false, isWritable = true),
      AccountMeta(destination, isSigner = false, isWritable = true),
      AccountMeta(authority, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.Transfer.index.toInt())
      .writeLongLe(amount.toLong())
    .readByteArray(),
  )

  public fun approve(
    source: PublicKey,
    `delegate`: PublicKey,
    owner: PublicKey,
    amount: ULong,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(source, isSigner = false, isWritable = true),
      AccountMeta(delegate, isSigner = false, isWritable = false),
      AccountMeta(owner, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.Approve.index.toInt())
      .writeLongLe(amount.toLong())
    .readByteArray(),
  )

  internal fun createApproveInstruction(
    source: PublicKey,
    `delegate`: PublicKey,
    owner: PublicKey,
    amount: ULong,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(source, isSigner = false, isWritable = true),
      AccountMeta(delegate, isSigner = false, isWritable = false),
      AccountMeta(owner, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.Approve.index.toInt())
      .writeLongLe(amount.toLong())
    .readByteArray(),
  )

  public fun revoke(source: PublicKey, owner: PublicKey): TransactionInstruction =
      createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(source, isSigner = false, isWritable = true),
      AccountMeta(owner, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.Revoke.index.toInt())
    .readByteArray(),
  )

  internal fun createRevokeInstruction(
    source: PublicKey,
    owner: PublicKey,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(source, isSigner = false, isWritable = true),
      AccountMeta(owner, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.Revoke.index.toInt())
    .readByteArray(),
  )

  public fun setAuthority(
    owned: PublicKey,
    owner: PublicKey,
    authorityType: Any,
    newAuthority: Any,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(owned, isSigner = false, isWritable = true),
      AccountMeta(owner, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.SetAuthority.index.toInt())
    .readByteArray(),
  )

  internal fun createSetAuthorityInstruction(
    owned: PublicKey,
    owner: PublicKey,
    authorityType: Any,
    newAuthority: Any,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(owned, isSigner = false, isWritable = true),
      AccountMeta(owner, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.SetAuthority.index.toInt())
    .readByteArray(),
  )

  public fun mintTo(
    mint: PublicKey,
    token: PublicKey,
    mintAuthority: PublicKey,
    amount: ULong,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(mint, isSigner = false, isWritable = true),
      AccountMeta(token, isSigner = false, isWritable = true),
      AccountMeta(mintAuthority, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.MintTo.index.toInt())
      .writeLongLe(amount.toLong())
    .readByteArray(),
  )

  internal fun createMintToInstruction(
    mint: PublicKey,
    token: PublicKey,
    mintAuthority: PublicKey,
    amount: ULong,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(mint, isSigner = false, isWritable = true),
      AccountMeta(token, isSigner = false, isWritable = true),
      AccountMeta(mintAuthority, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.MintTo.index.toInt())
      .writeLongLe(amount.toLong())
    .readByteArray(),
  )

  public fun burn(
    account: PublicKey,
    mint: PublicKey,
    authority: PublicKey,
    amount: ULong,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = true),
      AccountMeta(authority, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.Burn.index.toInt())
      .writeLongLe(amount.toLong())
    .readByteArray(),
  )

  internal fun createBurnInstruction(
    account: PublicKey,
    mint: PublicKey,
    authority: PublicKey,
    amount: ULong,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = true),
      AccountMeta(authority, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.Burn.index.toInt())
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
      AccountMeta(owner, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.CloseAccount.index.toInt())
    .readByteArray(),
  )

  internal fun createCloseAccountInstruction(
    account: PublicKey,
    destination: PublicKey,
    owner: PublicKey,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
      AccountMeta(destination, isSigner = false, isWritable = true),
      AccountMeta(owner, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.CloseAccount.index.toInt())
    .readByteArray(),
  )

  public fun freezeAccount(
    account: PublicKey,
    mint: PublicKey,
    owner: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(owner, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.FreezeAccount.index.toInt())
    .readByteArray(),
  )

  internal fun createFreezeAccountInstruction(
    account: PublicKey,
    mint: PublicKey,
    owner: PublicKey,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(owner, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.FreezeAccount.index.toInt())
    .readByteArray(),
  )

  public fun thawAccount(
    account: PublicKey,
    mint: PublicKey,
    owner: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(owner, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.ThawAccount.index.toInt())
    .readByteArray(),
  )

  internal fun createThawAccountInstruction(
    account: PublicKey,
    mint: PublicKey,
    owner: PublicKey,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(owner, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.ThawAccount.index.toInt())
    .readByteArray(),
  )

  public fun transferChecked(
    source: PublicKey,
    mint: PublicKey,
    destination: PublicKey,
    authority: PublicKey,
    amount: ULong,
    decimals: UByte,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(source, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(destination, isSigner = false, isWritable = true),
      AccountMeta(authority, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.TransferChecked.index.toInt())
      .writeLongLe(amount.toLong())
      .writeByte(decimals.toInt())
    .readByteArray(),
  )

  internal fun createTransferCheckedInstruction(
    source: PublicKey,
    mint: PublicKey,
    destination: PublicKey,
    authority: PublicKey,
    amount: ULong,
    decimals: UByte,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(source, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(destination, isSigner = false, isWritable = true),
      AccountMeta(authority, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.TransferChecked.index.toInt())
      .writeLongLe(amount.toLong())
      .writeByte(decimals.toInt())
    .readByteArray(),
  )

  public fun approveChecked(
    source: PublicKey,
    mint: PublicKey,
    `delegate`: PublicKey,
    owner: PublicKey,
    amount: ULong,
    decimals: UByte,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(source, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(delegate, isSigner = false, isWritable = false),
      AccountMeta(owner, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.ApproveChecked.index.toInt())
      .writeLongLe(amount.toLong())
      .writeByte(decimals.toInt())
    .readByteArray(),
  )

  internal fun createApproveCheckedInstruction(
    source: PublicKey,
    mint: PublicKey,
    `delegate`: PublicKey,
    owner: PublicKey,
    amount: ULong,
    decimals: UByte,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(source, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(delegate, isSigner = false, isWritable = false),
      AccountMeta(owner, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.ApproveChecked.index.toInt())
      .writeLongLe(amount.toLong())
      .writeByte(decimals.toInt())
    .readByteArray(),
  )

  public fun mintToChecked(
    mint: PublicKey,
    token: PublicKey,
    mintAuthority: PublicKey,
    amount: ULong,
    decimals: UByte,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(mint, isSigner = false, isWritable = true),
      AccountMeta(token, isSigner = false, isWritable = true),
      AccountMeta(mintAuthority, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.MintToChecked.index.toInt())
      .writeLongLe(amount.toLong())
      .writeByte(decimals.toInt())
    .readByteArray(),
  )

  internal fun createMintToCheckedInstruction(
    mint: PublicKey,
    token: PublicKey,
    mintAuthority: PublicKey,
    amount: ULong,
    decimals: UByte,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(mint, isSigner = false, isWritable = true),
      AccountMeta(token, isSigner = false, isWritable = true),
      AccountMeta(mintAuthority, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.MintToChecked.index.toInt())
      .writeLongLe(amount.toLong())
      .writeByte(decimals.toInt())
    .readByteArray(),
  )

  public fun burnChecked(
    account: PublicKey,
    mint: PublicKey,
    authority: PublicKey,
    amount: ULong,
    decimals: UByte,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = true),
      AccountMeta(authority, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.BurnChecked.index.toInt())
      .writeLongLe(amount.toLong())
      .writeByte(decimals.toInt())
    .readByteArray(),
  )

  internal fun createBurnCheckedInstruction(
    account: PublicKey,
    mint: PublicKey,
    authority: PublicKey,
    amount: ULong,
    decimals: UByte,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = true),
      AccountMeta(authority, isSigner = true, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.BurnChecked.index.toInt())
      .writeLongLe(amount.toLong())
      .writeByte(decimals.toInt())
    .readByteArray(),
  )

  public fun initializeAccount2(
    account: PublicKey,
    mint: PublicKey,
    owner: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(RENT, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.InitializeAccount2.index.toInt())
      .write(owner.bytes)
    .readByteArray(),
  )

  internal fun createInitializeAccount2Instruction(
    account: PublicKey,
    mint: PublicKey,
    owner: PublicKey,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = false),
      AccountMeta(RENT, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.InitializeAccount2.index.toInt())
      .write(owner.bytes)
    .readByteArray(),
  )

  public fun syncNative(account: PublicKey): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
    ),
    data = Buffer()
      .writeByte(Instruction.SyncNative.index.toInt())
    .readByteArray(),
  )

  internal fun createSyncNativeInstruction(account: PublicKey, programId: PublicKey):
      TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
    ),
    data = Buffer()
      .writeByte(Instruction.SyncNative.index.toInt())
    .readByteArray(),
  )

  public fun initializeAccount3(
    account: PublicKey,
    mint: PublicKey,
    owner: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.InitializeAccount3.index.toInt())
      .write(owner.bytes)
    .readByteArray(),
  )

  internal fun createInitializeAccount3Instruction(
    account: PublicKey,
    mint: PublicKey,
    owner: PublicKey,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
      AccountMeta(mint, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.InitializeAccount3.index.toInt())
      .write(owner.bytes)
    .readByteArray(),
  )

  public fun initializeMultisig2(multisig: PublicKey, m: UByte): TransactionInstruction =
      createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(multisig, isSigner = false, isWritable = true),
    ),
    data = Buffer()
      .writeByte(Instruction.InitializeMultisig2.index.toInt())
      .writeByte(m.toInt())
    .readByteArray(),
  )

  internal fun createInitializeMultisig2Instruction(
    multisig: PublicKey,
    m: UByte,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(multisig, isSigner = false, isWritable = true),
    ),
    data = Buffer()
      .writeByte(Instruction.InitializeMultisig2.index.toInt())
      .writeByte(m.toInt())
    .readByteArray(),
  )

  public fun initializeMint2(
    mint: PublicKey,
    decimals: UByte,
    mintAuthority: PublicKey,
    freezeAuthority: Any,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(mint, isSigner = false, isWritable = true),
    ),
    data = Buffer()
      .writeByte(Instruction.InitializeMint2.index.toInt())
      .writeByte(decimals.toInt())
      .write(mintAuthority.bytes)
    .readByteArray(),
  )

  internal fun createInitializeMint2Instruction(
    mint: PublicKey,
    decimals: UByte,
    mintAuthority: PublicKey,
    freezeAuthority: Any,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(mint, isSigner = false, isWritable = true),
    ),
    data = Buffer()
      .writeByte(Instruction.InitializeMint2.index.toInt())
      .writeByte(decimals.toInt())
      .write(mintAuthority.bytes)
    .readByteArray(),
  )

  public fun getAccountDataSize(mint: PublicKey): TransactionInstruction =
      createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(mint, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.GetAccountDataSize.index.toInt())
    .readByteArray(),
  )

  internal fun createGetAccountDataSizeInstruction(mint: PublicKey, programId: PublicKey):
      TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(mint, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.GetAccountDataSize.index.toInt())
    .readByteArray(),
  )

  public fun initializeImmutableOwner(account: PublicKey): TransactionInstruction =
      createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
    ),
    data = Buffer()
      .writeByte(Instruction.InitializeImmutableOwner.index.toInt())
    .readByteArray(),
  )

  internal fun createInitializeImmutableOwnerInstruction(account: PublicKey, programId: PublicKey):
      TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(account, isSigner = false, isWritable = true),
    ),
    data = Buffer()
      .writeByte(Instruction.InitializeImmutableOwner.index.toInt())
    .readByteArray(),
  )

  public fun amountToUiAmount(mint: PublicKey, amount: ULong): TransactionInstruction =
      createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(mint, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.AmountToUiAmount.index.toInt())
      .writeLongLe(amount.toLong())
    .readByteArray(),
  )

  internal fun createAmountToUiAmountInstruction(
    mint: PublicKey,
    amount: ULong,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(mint, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.AmountToUiAmount.index.toInt())
      .writeLongLe(amount.toLong())
    .readByteArray(),
  )

  public fun uiAmountToAmount(mint: PublicKey, uiAmount: String): TransactionInstruction =
      createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(mint, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.UiAmountToAmount.index.toInt())
    .readByteArray(),
  )

  internal fun createUiAmountToAmountInstruction(
    mint: PublicKey,
    uiAmount: String,
    programId: PublicKey,
  ): TransactionInstruction = createTransactionInstruction(
    programId = programId,
    keys = listOf(
      AccountMeta(mint, isSigner = false, isWritable = false),
    ),
    data = Buffer()
      .writeByte(Instruction.UiAmountToAmount.index.toInt())
    .readByteArray(),
  )

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
    InitializeMultisig2(19u),
    InitializeMint2(20u),
    GetAccountDataSize(21u),
    InitializeImmutableOwner(22u),
    AmountToUiAmount(23u),
    UiAmountToAmount(24u),
    ;
  }
}
