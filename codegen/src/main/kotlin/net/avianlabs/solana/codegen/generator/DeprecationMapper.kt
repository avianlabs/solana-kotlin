package net.avianlabs.solana.codegen.generator

object DeprecationMapper {
  
  data class DeprecatedFunction(
  val oldName: String,
  val newName: String,
  val paramMapping: Map<String, String>
  )
  
  val deprecatedFunctions = listOf(
  DeprecatedFunction(
    oldName = "transfer",
    newName = "transferSol",
    paramMapping = mapOf(
    "source" to "fromPublicKey",
    "destination" to "toPublicKey",
    "amount" to "lamports"
    )
  )
  )
  
  fun getDeprecationForInstruction(instructionName: String): DeprecatedFunction? {
  return deprecatedFunctions.find { it.newName == instructionName }
  }
}
