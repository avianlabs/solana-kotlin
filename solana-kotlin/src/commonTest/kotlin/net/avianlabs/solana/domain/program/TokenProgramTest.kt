package net.avianlabs.solana.domain.program

import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import kotlin.test.Test
import kotlin.test.assertEquals

class TokenProgramTest {

  @Test
  fun polymorphicTransferChecked() {
    fun buildTransfer(program: TokenProgram) =
      program.transferChecked(
        source = PublicKey(ByteArray(32)),
        mint = PublicKey(ByteArray(32)),
        destination = PublicKey(ByteArray(32)),
        authority = PublicKey(ByteArray(32)),
        amount = 100UL,
        decimals = 9u.toUByte(),
      )

    val tokenIx = buildTransfer(TokenProgram)
    val token2022Ix = buildTransfer(Token2022Program)
    assertEquals(TokenProgram.programId, tokenIx.programId)
    assertEquals(Token2022Program.programId, token2022Ix.programId)
  }

  @Test
  fun polymorphicSetAuthority() {
    fun buildSetAuthority(
      program: TokenProgram,
      authorityType: TokenProgram.AuthorityType,
    ) = program.setAuthority(
      owned = PublicKey(ByteArray(32)),
      owner = PublicKey(ByteArray(32)),
      authorityType = authorityType,
      newAuthority = null,
    )

    val tokenIx = buildSetAuthority(TokenProgram, TokenProgram.AuthorityType.MintTokens)
    val token2022Ix = buildSetAuthority(
      Token2022Program,
      TokenProgram.AuthorityType.TransferFeeConfig,
    )
    assertEquals(TokenProgram.programId, tokenIx.programId)
    assertEquals(Token2022Program.programId, token2022Ix.programId)
  }
}
