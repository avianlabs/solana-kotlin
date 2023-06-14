package net.avianlabs.solana.domain.program

import net.avianlabs.solana.domain.core.PublicKey

public object TokenProgram : Program() {

  public val PROGRAM_ID: PublicKey =
    PublicKey.fromBase58("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA")
  public val SYSVAR_RENT_PUBKEY: PublicKey =
    PublicKey.fromBase58("SysvarRent111111111111111111111111111111111")
  public val INITIALIZE_MINT_ID: UByte = 0u
  public val INITIALIZE_METHOD_ID: UByte = 1u
  public val TRANSFER_METHOD_ID: UByte = 3u
  public val MINT_TO_METHOD_ID: UByte = 7u
  public val CLOSE_ACCOUNT_METHOD_ID: UByte = 9u
  public val TRANSFER_CHECKED_METHOD_ID: UByte = 12u
}
