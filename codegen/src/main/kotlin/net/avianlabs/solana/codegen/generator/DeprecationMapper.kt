package net.avianlabs.solana.codegen.generator

object DeprecationMapper {

  data class DeprecatedFunction(
    val oldName: String,
    val newName: String,
    val paramMapping: Map<String, String>,
    // Extra params in old signature that don't exist in new (name to default value expression)
    val extraOldParams: List<ExtraParam> = emptyList(),
    // Default values for account params in the deprecated shim (account camelCase name -> default expression)
    val accountDefaults: Map<String, String> = emptyMap(),
    // When true, keeps unsigned types (UInt, ULong, etc.) instead of mapping to signed counterparts
    val keepUnsignedTypes: Boolean = false,
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
      DeprecatedConstant(
        "RECENT_BLOCKHASHES_SYSVAR",
        "SYSVAR_RECENT_BLOCKHASH",
        ConstantType.PUBLIC_KEY
      ),
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
      ),
      accountDefaults = mapOf(
        "recentBlockhashesSysvar" to "RECENT_BLOCKHASHES_SYSVAR"
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
    // Note: No shim for setComputeUnitLimit. A UInt→UInt shim would conflict
    // with the new setComputeUnitLimit(units: UInt) signature. Callers just
    // need to rename maxUnits= to units=.
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
      ),
      accountDefaults = mapOf(
        "systemProgram" to "SystemProgram.programId"
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
      ),
      accountDefaults = mapOf(
        "systemProgram" to "SystemProgram.programId"
      )
    ),
  )

  fun getDeprecationsForInstruction(instructionName: String): List<DeprecatedFunction> {
    return deprecatedFunctions.filter { it.newName == instructionName }
  }
}
