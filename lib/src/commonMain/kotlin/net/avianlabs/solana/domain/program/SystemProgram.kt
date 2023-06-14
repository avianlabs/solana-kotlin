package net.avianlabs.solana.domain.program

import net.avianlabs.solana.domain.core.PublicKey

public object SystemProgram : Program() {

  public val PROGRAM_ID: PublicKey = PublicKey.fromBase58("11111111111111111111111111111111")
  public const val PROGRAM_INDEX_CREATE_ACCOUNT: Long = 0L
  public const val PROGRAM_INDEX_TRANSFER: Long = 2L
}
