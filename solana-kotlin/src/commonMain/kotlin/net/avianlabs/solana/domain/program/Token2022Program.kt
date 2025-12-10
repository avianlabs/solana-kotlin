package net.avianlabs.solana.domain.program

import net.avianlabs.solana.domain.core.TransactionInstruction
import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

public object Token2022Program : Program {
    public override val programId: PublicKey =
        PublicKey.fromBase58("TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb")
    
    public fun transfer(
        source: PublicKey,
        destination: PublicKey,
        authority: PublicKey,
        amount: ULong,
    ): TransactionInstruction = TokenProgram.createTransferInstruction(
        source = source,
        destination = destination,
        authority = authority,
        amount = amount,
        programId = programId
    )
    
    public fun approve(
        source: PublicKey,
        delegate: PublicKey,
        owner: PublicKey,
        amount: ULong,
    ): TransactionInstruction = TokenProgram.createApproveInstruction(
        source = source,
        delegate = delegate,
        owner = owner,
        amount = amount,
        programId = programId
    )
    
    public fun revoke(
        source: PublicKey,
        owner: PublicKey,
    ): TransactionInstruction = TokenProgram.createRevokeInstruction(
        source = source,
        owner = owner,
        programId = programId
    )
    
    public fun closeAccount(
        account: PublicKey,
        destination: PublicKey,
        owner: PublicKey,
    ): TransactionInstruction = TokenProgram.createCloseAccountInstruction(
        account = account,
        destination = destination,
        owner = owner,
        programId = programId
    )
    
    public fun transferChecked(
        source: PublicKey,
        mint: PublicKey,
        destination: PublicKey,
        authority: PublicKey,
        amount: ULong,
        decimals: UByte,
    ): TransactionInstruction = TokenProgram.createTransferCheckedInstruction(
        source = source,
        mint = mint,
        destination = destination,
        authority = authority,
        amount = amount,
        decimals = decimals,
        programId = programId
    )
}
