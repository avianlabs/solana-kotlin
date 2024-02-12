package net.avianlabs.solana.domain.program

import net.avianlabs.solana.domain.core.PublicKey
import net.avianlabs.solana.domain.core.TransactionInstruction

private val TOKEN_PROGRAM_ID = PublicKey.fromBase58("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA")

public object TokenProgram : TokenProgramBase(TOKEN_PROGRAM_ID) {

  public fun createAssociatedTokenAccountInstruction(
    mint: PublicKey,
    associatedAccount: PublicKey,
    owner: PublicKey,
    payer: PublicKey,
  ): TransactionInstruction = AssociatedTokenProgram.createAssociatedTokenAccountInstruction(
    programId = programId,
    mint = mint,
    associatedAccount = associatedAccount,
    owner = owner,
    payer = payer,
  )
}
