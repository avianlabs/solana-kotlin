package net.avianlabs.solana.domain.program

import net.avianlabs.solana.tweetnacl.ed25519.PublicKey

public fun PublicKey.associatedTokenAddress(
  tokenMintAddress: PublicKey,
  programId: PublicKey = TokenProgram.programId,
): ProgramDerivedAddress = Program.findProgramAddress(
  listOf(
    bytes.copyOf(),
    programId.toByteArray(),
    tokenMintAddress.toByteArray()
  ),
  AssociatedTokenProgram.programId,
)
