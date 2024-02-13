package net.avianlabs.solana.domain.program

import net.avianlabs.solana.domain.core.PublicKey
import net.avianlabs.solana.domain.core.TransactionInstruction

private val TOKEN_2022_PROGRAM_ID =
  PublicKey.fromBase58("TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb")

public object Token2022Program : TokenProgramBase(TOKEN_2022_PROGRAM_ID) {

  public enum class Extensions(
    public val index: UByte,
  ) {
    TransferFeeExtension(26u),
    ConfidentialTransferExtension(27u),
    DefaultAccountStateExtension(28u),
    Reallocate(29u),
    MemoTransferExtension(30u),
    CreateNativeMint(31u),
    InitializeNonTransferableMint(32u),
    InterestBearingMintExtension(33u),
    CpiGuardExtension(34u),
    InitializePermanentDelegate(35u),
    TransferHookExtension(36u),
    ConfidentialTransferFeeExtension(37u),
    WithdrawalExcessLamports(38u),
    MetadataPointerExtension(39u),
  }

  @Deprecated(
    message = "Token2022Program does not support transfer, use transferChecked instead",
    replaceWith = ReplaceWith("transferChecked(source, destination, owner, amount, decimals, mint)"),
    level = DeprecationLevel.ERROR,
  )
  public override fun transfer(
    source: PublicKey,
    destination: PublicKey,
    owner: PublicKey,
    amount: ULong,
  ): TransactionInstruction =
    error("Token2022Program does not support transfer, use transferChecked instead")
}
