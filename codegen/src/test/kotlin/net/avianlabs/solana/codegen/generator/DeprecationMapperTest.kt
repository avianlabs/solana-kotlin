package net.avianlabs.solana.codegen.generator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DeprecationMapperTest {

  @Test
  fun `getDeprecationsForInstruction returns mapping for transferSol`() {
    val deprecations = DeprecationMapper.getDeprecationsForInstruction("transferSol")
    assertEquals(1, deprecations.size)
    val deprecation = deprecations.first()
    assertEquals("transfer", deprecation.oldName)
    assertEquals("transferSol", deprecation.newName)
    assertEquals("fromPublicKey", deprecation.paramMapping["source"])
    assertEquals("toPublicKey", deprecation.paramMapping["destination"])
    assertEquals("lamports", deprecation.paramMapping["amount"])
  }

  @Test
  fun `getDeprecationsForInstruction returns mapping for advanceNonceAccount`() {
    val deprecations = DeprecationMapper.getDeprecationsForInstruction("advanceNonceAccount")
    assertEquals(1, deprecations.size)
    val deprecation = deprecations.first()
    assertEquals("nonceAdvance", deprecation.oldName)
    assertEquals("authorized", deprecation.paramMapping["nonceAuthority"])
    assertEquals("RECENT_BLOCKHASHES_SYSVAR", deprecation.accountDefaults["recentBlockhashesSysvar"])
  }

  @Test
  fun `getDeprecationsForInstruction returns mapping for initializeNonceAccount`() {
    val deprecations = DeprecationMapper.getDeprecationsForInstruction("initializeNonceAccount")
    assertEquals(1, deprecations.size)
    assertEquals("nonceInitialize", deprecations.first().oldName)
  }

  @Test
  fun `getDeprecationsForInstruction returns empty for unknown instruction`() {
    assertTrue(DeprecationMapper.getDeprecationsForInstruction("unknownInstruction").isEmpty())
  }

  @Test
  fun `getDeprecationsForInstruction returns mapping for createAssociatedToken`() {
    val deprecations = DeprecationMapper.getDeprecationsForInstruction("createAssociatedToken")
    assertEquals(1, deprecations.size)
    val deprecation = deprecations.first()
    assertEquals("createAssociatedTokenAccountInstruction", deprecation.oldName)
    assertEquals("associatedAccount", deprecation.paramMapping["ata"])
    assertEquals("programId", deprecation.paramMapping["tokenProgram"])
    assertEquals(1, deprecation.extraOldParams.size)
    assertEquals("associatedProgramId", deprecation.extraOldParams[0].name)
    assertEquals("this.programId", deprecation.extraOldParams[0].defaultValue)
  }

  @Test
  fun `getDeprecationsForInstruction returns mapping for createAssociatedTokenIdempotent`() {
    val deprecations = DeprecationMapper.getDeprecationsForInstruction("createAssociatedTokenIdempotent")
    assertEquals(1, deprecations.size)
    assertEquals("createAssociatedTokenAccountInstructionIdempotent", deprecations.first().oldName)
  }

  @Test
  fun `getDeprecationsForInstruction returns no mapping for setComputeUnitLimit`() {
    // No shim for setComputeUnitLimit — a UInt→UInt shim would conflict with the new signature
    val deprecations = DeprecationMapper.getDeprecationsForInstruction("setComputeUnitLimit")
    assertEquals(0, deprecations.size)
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
