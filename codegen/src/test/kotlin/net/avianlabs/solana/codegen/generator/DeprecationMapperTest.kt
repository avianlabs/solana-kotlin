package net.avianlabs.solana.codegen.generator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DeprecationMapperTest {

  @Test
  fun `getDeprecationForInstruction returns mapping for transferSol`() {
    val deprecation = DeprecationMapper.getDeprecationForInstruction("transferSol")
    assertNotNull(deprecation)
    assertEquals("transfer", deprecation.oldName)
    assertEquals("transferSol", deprecation.newName)
    assertEquals("fromPublicKey", deprecation.paramMapping["source"])
    assertEquals("toPublicKey", deprecation.paramMapping["destination"])
    assertEquals("lamports", deprecation.paramMapping["amount"])
  }

  @Test
  fun `getDeprecationForInstruction returns mapping for advanceNonceAccount`() {
    val deprecation = DeprecationMapper.getDeprecationForInstruction("advanceNonceAccount")
    assertNotNull(deprecation)
    assertEquals("nonceAdvance", deprecation.oldName)
    assertEquals("authorized", deprecation.paramMapping["nonceAuthority"])
  }

  @Test
  fun `getDeprecationForInstruction returns mapping for initializeNonceAccount`() {
    val deprecation = DeprecationMapper.getDeprecationForInstruction("initializeNonceAccount")
    assertNotNull(deprecation)
    assertEquals("nonceInitialize", deprecation.oldName)
  }

  @Test
  fun `getDeprecationForInstruction returns null for unknown instruction`() {
    assertNull(DeprecationMapper.getDeprecationForInstruction("unknownInstruction"))
  }

  @Test
  fun `getDeprecationForInstruction returns mapping for createAssociatedToken`() {
    val deprecation = DeprecationMapper.getDeprecationForInstruction("createAssociatedToken")
    assertNotNull(deprecation)
    assertEquals("createAssociatedTokenAccountInstruction", deprecation.oldName)
    assertEquals("associatedAccount", deprecation.paramMapping["ata"])
    assertEquals("programId", deprecation.paramMapping["tokenProgram"])
    assertEquals(1, deprecation.extraOldParams.size)
    assertEquals("associatedProgramId", deprecation.extraOldParams[0].name)
    assertEquals("this.programId", deprecation.extraOldParams[0].defaultValue)
  }

  @Test
  fun `getDeprecationForInstruction returns mapping for createAssociatedTokenIdempotent`() {
    val deprecation = DeprecationMapper.getDeprecationForInstruction("createAssociatedTokenIdempotent")
    assertNotNull(deprecation)
    assertEquals("createAssociatedTokenAccountInstructionIdempotent", deprecation.oldName)
  }

  @Test
  fun `deprecated constants for system program`() {
    val constants = DeprecationMapper.getDeprecatedConstantsForProgram("system")
    assertEquals(3, constants.size)

    val rentSysvar = constants.find { it.oldName == "SYSVAR_RENT_ACCOUNT" }
    assertNotNull(rentSysvar)
    assertEquals("RENT_SYSVAR", rentSysvar.newName)
    assertEquals(DeprecationMapper.ConstantType.PUBLIC_KEY, rentSysvar.type)

    val recentBlockhashes = constants.find { it.oldName == "SYSVAR_RECENT_BLOCKHASH" }
    assertNotNull(recentBlockhashes)
    assertEquals("RECENT_BLOCKHASHES_SYSVAR", recentBlockhashes.newName)

    val nonceLength = constants.find { it.oldName == "NONCE_ACCOUNT_LENGTH" }
    assertNotNull(nonceLength)
    assertEquals("NONCE_LENGTH", nonceLength.newName)
    assertEquals(DeprecationMapper.ConstantType.LONG, nonceLength.type)
  }

  @Test
  fun `deprecated constants for unknown program returns empty`() {
    val constants = DeprecationMapper.getDeprecatedConstantsForProgram("unknown")
    assertEquals(0, constants.size)
  }
}
