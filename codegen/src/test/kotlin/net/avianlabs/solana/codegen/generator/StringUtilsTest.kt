package net.avianlabs.solana.codegen.generator

import kotlin.test.Test
import kotlin.test.assertEquals

class StringUtilsTest {

  @Test
  fun `toPascalCase converts snake_case`() {
    assertEquals("TransferSol", "transfer_sol".toPascalCase())
  }

  @Test
  fun `toPascalCase converts kebab-case`() {
    assertEquals("ComputeBudget", "compute-budget".toPascalCase())
  }

  @Test
  fun `toPascalCase preserves already PascalCase`() {
    assertEquals("TransferSol", "TransferSol".toPascalCase())
  }

  @Test
  fun `toPascalCase handles single word`() {
    assertEquals("Transfer", "transfer".toPascalCase())
  }

  @Test
  fun `toPascalCase handles numeric suffix`() {
    assertEquals("Token2022", "token_2022".toPascalCase())
  }

  @Test
  fun `toCamelCase converts snake_case`() {
    assertEquals("transferSol", "transfer_sol".toCamelCase())
  }

  @Test
  fun `toCamelCase converts kebab-case`() {
    assertEquals("computeBudget", "compute-budget".toCamelCase())
  }

  @Test
  fun `toCamelCase handles single word`() {
    assertEquals("transfer", "transfer".toCamelCase())
  }

  @Test
  fun `toScreamingSnakeCase converts snake_case`() {
    assertEquals("TRANSFER_SOL", "transfer_sol".toScreamingSnakeCase())
  }

  @Test
  fun `toScreamingSnakeCase converts camelCase`() {
    assertEquals("TRANSFER_SOL", "transferSol".toScreamingSnakeCase())
  }

  @Test
  fun `toScreamingSnakeCase converts kebab-case`() {
    assertEquals("COMPUTE_BUDGET", "compute-budget".toScreamingSnakeCase())
  }

  @Test
  fun `toScreamingSnakeCase converts PascalCase`() {
    assertEquals("TRANSFER_SOL", "TransferSol".toScreamingSnakeCase())
  }

  @Test
  fun `toScreamingSnakeCase handles single word`() {
    assertEquals("TRANSFER", "transfer".toScreamingSnakeCase())
  }

  @Test
  fun `toScreamingSnakeCase handles consecutive uppercase`() {
    assertEquals("SYSVAR_RENT", "sysvarRent".toScreamingSnakeCase())
  }
}
