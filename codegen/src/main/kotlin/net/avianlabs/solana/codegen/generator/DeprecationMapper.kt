package net.avianlabs.solana.codegen.generator

object DeprecationMapper {

  data class DeprecatedFunction(
    val oldName: String,
    val newName: String,
    val paramMapping: Map<String, String>,
    // Extra params in old signature that don't exist in new (name to default value expression)
    val extraOldParams: List<ExtraParam> = emptyList()
  )

  data class ExtraParam(
    val name: String,
    val type: ParamType,
    val defaultValue: String? = null  // null means required, non-null means optional with this default
  )

  enum class ParamType { PUBLIC_KEY, LONG }

  data class DeprecatedConstant(
    val newName: String,
    val oldName: String,
    val type: ConstantType
  )

  enum class ConstantType { PUBLIC_KEY, LONG }

  val deprecatedConstants: Map<String, List<DeprecatedConstant>> = mapOf(
    "system" to listOf(
      DeprecatedConstant("RENT_SYSVAR", "SYSVAR_RENT_ACCOUNT", ConstantType.PUBLIC_KEY),
      DeprecatedConstant("RECENT_BLOCKHASHES_SYSVAR", "SYSVAR_RECENT_BLOCKHASH", ConstantType.PUBLIC_KEY),
      DeprecatedConstant("NONCE_LENGTH", "NONCE_ACCOUNT_LENGTH", ConstantType.LONG),
    )
  )

  fun getDeprecatedConstantsForProgram(programName: String): List<DeprecatedConstant> {
    return deprecatedConstants[programName] ?: emptyList()
  }

  val deprecatedFunctions = listOf(
    // SystemProgram
    DeprecatedFunction(
      oldName = "transfer",
      newName = "transferSol",
      paramMapping = mapOf(
        "source" to "fromPublicKey",
        "destination" to "toPublicKey",
        "amount" to "lamports"
      )
    ),
    DeprecatedFunction(
      oldName = "nonceAdvance",
      newName = "advanceNonceAccount",
      paramMapping = mapOf(
        "nonceAuthority" to "authorized"
      )
    ),
    DeprecatedFunction(
      oldName = "nonceInitialize",
      newName = "initializeNonceAccount",
      paramMapping = mapOf(
        "nonceAuthority" to "authorized"
      )
    ),
    // ComputeBudgetProgram
    DeprecatedFunction(
      oldName = "setComputeUnitLimit",
      newName = "setComputeUnitLimit",
      paramMapping = mapOf(
        "units" to "maxUnits"
      )
    ),
    // AssociatedTokenProgram
    DeprecatedFunction(
      oldName = "createAssociatedTokenAccountInstruction",
      newName = "createAssociatedToken",
      paramMapping = mapOf(
        "ata" to "associatedAccount",
        "tokenProgram" to "programId"
      ),
      extraOldParams = listOf(
        ExtraParam("associatedProgramId", ParamType.PUBLIC_KEY, "this.programId")
      )
    ),
    DeprecatedFunction(
      oldName = "createAssociatedTokenAccountInstructionIdempotent",
      newName = "createAssociatedTokenIdempotent",
      paramMapping = mapOf(
        "ata" to "associatedAccount",
        "tokenProgram" to "programId"
      ),
      extraOldParams = listOf(
        ExtraParam("associatedProgramId", ParamType.PUBLIC_KEY, "this.programId")
      )
    ),
  )

  fun getDeprecationForInstruction(instructionName: String): DeprecatedFunction? {
    return deprecatedFunctions.find { it.newName == instructionName }
  }
}
