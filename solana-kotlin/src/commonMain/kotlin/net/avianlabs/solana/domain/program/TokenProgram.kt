package net.avianlabs.solana.domain.program

import net.avianlabs.solana.domain.core.PublicKey

private val TOKEN_PROGRAM_ID = PublicKey.fromBase58("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA")

public object TokenProgram : TokenProgramBase(TOKEN_PROGRAM_ID)
