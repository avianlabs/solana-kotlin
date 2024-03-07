package net.avianlabs.solana.domain.program

import net.avianlabs.solana.tweetnacl.crypto.PublicKey
import net.avianlabs.solana.domain.core.TransactionInstruction
import net.avianlabs.solana.domain.program.TokenProgram.Companion.createCloseAccountInstruction
import net.avianlabs.solana.domain.program.TokenProgram.Companion.createTransferCheckedInstruction


public interface Token2022Program : TokenProgram {

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

  public companion object : Token2022Program, TokenProgram by TokenProgram.Companion {

    public override val programId: PublicKey =
      PublicKey.fromBase58("TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb")

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
  }
}
