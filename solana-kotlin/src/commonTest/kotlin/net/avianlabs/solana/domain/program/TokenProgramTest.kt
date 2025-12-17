package net.avianlabs.solana.domain.program

import net.avianlabs.solana.tweetnacl.ed25519.PublicKey
import kotlin.test.Test
import kotlin.test.assertEquals

class TokenProgramTest {

  @Test
  fun testTokenProgramIds() {
    val tokenTransfer = TokenProgram.transferChecked(
      source = PublicKey(ByteArray(32)),
      destination = PublicKey(ByteArray(32)),
      authority = PublicKey(ByteArray(32)),
      amount = 0UL,
      decimals = 0u.toUByte(),
      mint = PublicKey(ByteArray(32)),
    )

    assertEquals(
      tokenTransfer.programId,
      TokenProgram.programId,
    )
  }

  @Test
  fun testToken2022ProgramIds() {
    val token2022Transfer = Token2022Program.transferChecked(
      source = PublicKey(ByteArray(32)),
      destination = PublicKey(ByteArray(32)),
      authority = PublicKey(ByteArray(32)),
      amount = 0UL,
      decimals = 0u.toUByte(),
      mint = PublicKey(ByteArray(32)),
    )

    assertEquals(
      token2022Transfer.programId,
      Token2022Program.programId,
    )
  }
}
