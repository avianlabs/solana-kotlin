package net.avianlabs.solana.codegen.generator

import com.squareup.kotlinpoet.*
import net.avianlabs.solana.codegen.idl.InstructionArgumentNode
import net.avianlabs.solana.codegen.idl.InstructionNode

class DeprecatedFunctionGenerator(
  private val instruction: InstructionNode,
  private val deprecation: DeprecationMapper.DeprecatedFunction
) {

  fun generate(): FunSpec {
    val nonDiscriminatorArgs = instruction.arguments.filter { arg ->
      instruction.discriminators.none { it.name == arg.name }
    }

    return FunSpec.builder(deprecation.oldName)
      .addModifiers(KModifier.PUBLIC)
      .addAnnotation(buildDeprecationAnnotation())
      .apply {
        addParameters(nonDiscriminatorArgs)
      }
      .returns(ClassName("net.avianlabs.solana.domain.core", "TransactionInstruction"))
      .addStatement(
        "return %L(%L)",
        instruction.name.toCamelCase(),
        buildParameterList(nonDiscriminatorArgs)
      )
      .build()
  }

  private fun buildDeprecationAnnotation(): AnnotationSpec {
    val newParamList = buildNewParameterList()

    return AnnotationSpec.builder(Deprecated::class)
      .addMember("message = %S", "Use ${instruction.name.toCamelCase()} instead")
      .addMember(
        "replaceWith = ReplaceWith(%S)",
        "${instruction.name.toCamelCase()}($newParamList)"
      )
      .build()
  }

  private fun FunSpec.Builder.addParameters(args: List<InstructionArgumentNode>) {
    deprecation.extraOldParams.filter { it.defaultValue != null }.forEach { extra ->
      val type = when (extra.type) {
        DeprecationMapper.ParamType.PUBLIC_KEY ->
          ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")

        DeprecationMapper.ParamType.LONG -> LONG
      }
      addParameter(
        ParameterSpec.builder(extra.name, type)
          .defaultValue(extra.defaultValue!!)
          .build()
      )
    }
    deprecation.extraOldParams.filter { it.defaultValue == null }.forEach { extra ->
      val type = when (extra.type) {
        DeprecationMapper.ParamType.PUBLIC_KEY -> ClassName(
          "net.avianlabs.solana.tweetnacl.ed25519",
          "PublicKey"
        )

        DeprecationMapper.ParamType.LONG -> LONG
      }
      addParameter(extra.name, type)
    }
    // Add accounts without defaults first, then accounts with defaults (for positional arg compat)
    val (accountsWithDefaults, accountsWithoutDefaults) = instruction.accounts.partition {
      deprecation.accountDefaults.containsKey(it.name.toCamelCase())
    }
    accountsWithoutDefaults.forEach { account ->
      val oldParamName = deprecation.paramMapping[account.name.toCamelCase()]
        ?: account.name.toCamelCase()
      addParameter(
        oldParamName,
        ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
      )
    }
    args.forEach { arg ->
      val oldParamName = deprecation.paramMapping[arg.name.toCamelCase()]
        ?: arg.name.toCamelCase()
      val argType = mapArgType(arg)
      addParameter(oldParamName, argType)
    }
    accountsWithDefaults.forEach { account ->
      val oldParamName = deprecation.paramMapping[account.name.toCamelCase()]
        ?: account.name.toCamelCase()
      val defaultValue = deprecation.accountDefaults[account.name.toCamelCase()]!!
      addParameter(
        ParameterSpec.builder(
          oldParamName,
          ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
        ).defaultValue(defaultValue).build()
      )
    }
  }

  private fun buildNewParameterList(): String {
    val params = mutableListOf<String>()

    instruction.accounts.forEach { account ->
      val oldName = deprecation.paramMapping[account.name.toCamelCase()]
        ?: account.name.toCamelCase()
      params.add("${account.name.toCamelCase()} = $oldName")
    }

    instruction.arguments.filter { arg ->
      instruction.discriminators.none { it.name == arg.name }
    }.forEach { arg ->
      val conversion = castOldTypes(arg)
      params.add("${arg.name.toCamelCase()} = $conversion")
    }

    return params.joinToString(", ")
  }

  private fun buildParameterList(args: List<InstructionArgumentNode>): String {
    val params = mutableListOf<String>()

    instruction.accounts.forEach { account ->
      val oldName = deprecation.paramMapping[account.name.toCamelCase()]
        ?: account.name.toCamelCase()
      params.add("${account.name.toCamelCase()} = $oldName")
    }

    args.forEach { arg ->
      val conversion = castOldTypes(arg)
      params.add("${arg.name.toCamelCase()} = $conversion")
    }

    return params.joinToString(", ")
  }

  /**
   * Maps IDL types to legacy (pre-codegen) Kotlin types for deprecated shims.
   * Unsigned IDL types map to their signed counterparts since the old API used
   * signed types, unless [DeprecationMapper.DeprecatedFunction.keepUnsignedTypes]
   * is set, in which case unsigned types are preserved as-is.
   */
  private fun mapArgType(arg: InstructionArgumentNode): TypeName = when (arg.type.kind) {
    "publicKeyTypeNode" -> ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
    "numberTypeNode" -> if (deprecation.keepUnsignedTypes) {
      when (arg.type.format) {
        "u8" -> U_BYTE
        "i8" -> BYTE
        "u16" -> U_SHORT
        "i16" -> SHORT
        "u32" -> U_INT
        "i32" -> INT
        "u64" -> U_LONG
        "i64" -> LONG
        else -> LONG
      }
    } else {
      when (arg.type.format) {
        "u8", "i8" -> BYTE
        "u16", "i16" -> SHORT
        "u32", "i32" -> INT
        "u64", "i64" -> LONG
        else -> LONG
      }
    }
    "booleanTypeNode" -> BOOLEAN
    "stringTypeNode" -> STRING
    else -> LONG
  }

  private fun castOldTypes(arg: InstructionArgumentNode): String {
    val oldName = deprecation.paramMapping[arg.name.toCamelCase()]
      ?: arg.name.toCamelCase()
    if (deprecation.keepUnsignedTypes) return oldName
    val conversion = when (arg.type.format) {
      "u64" -> "$oldName.toULong()"
      "u8" -> "$oldName.toUByte()"
      "u16" -> "$oldName.toUShort()"
      "u32" -> "$oldName.toUInt()"
      else -> oldName
    }
    return conversion
  }

  private fun String.toCamelCase(): String {
    val pascal = split('_', '-')
      .joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }
    return pascal.replaceFirstChar(Char::lowercaseChar)
  }
}
