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
    // Add extra old params with defaults first (they came first in the old signature)
    deprecation.extraOldParams.filter { it.defaultValue != null }.forEach { extra ->
      val type = when (extra.type) {
        DeprecationMapper.ParamType.PUBLIC_KEY -> ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
        DeprecationMapper.ParamType.LONG -> LONG
      }
      addParameter(
        ParameterSpec.builder(extra.name, type)
          .defaultValue(extra.defaultValue!!)
          .build()
      )
    }

    // Add extra old params without defaults (required params)
    deprecation.extraOldParams.filter { it.defaultValue == null }.forEach { extra ->
      val type = when (extra.type) {
        DeprecationMapper.ParamType.PUBLIC_KEY -> ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
        DeprecationMapper.ParamType.LONG -> LONG
      }
      addParameter(extra.name, type)
    }

    // Add account params
    instruction.accounts.forEach { account ->
      val oldParamName = deprecation.paramMapping[account.name.toCamelCase()]
        ?: account.name.toCamelCase()
      addParameter(
        oldParamName,
        ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
      )
    }

    // Add argument params
    args.forEach { arg ->
      val oldParamName = deprecation.paramMapping[arg.name.toCamelCase()]
        ?: arg.name.toCamelCase()
      val argType = when (arg.type.kind) {
        "publicKeyTypeNode" -> ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
        "numberTypeNode" -> when (arg.type.format) {
          "u8" -> BYTE
          "u16" -> SHORT
          "u32" -> INT
          "u64" -> LONG
          "i8" -> BYTE
          "i16" -> SHORT
          "i32" -> INT
          "i64" -> LONG
          "f32" -> FLOAT
          "f64" -> DOUBLE
          else -> LONG
        }
        "booleanTypeNode" -> BOOLEAN
        else -> LONG
      }
      addParameter(oldParamName, argType)
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
      val oldName = deprecation.paramMapping[arg.name.toCamelCase()]
        ?: arg.name.toCamelCase()
      val conversion = when (arg.type.format) {
        "u64" -> "$oldName.toULong()"
        "u8" -> "$oldName.toUByte()"
        "u16" -> "$oldName.toUShort()"
        "u32" -> "$oldName.toUInt()"
        else -> oldName
      }
      params.add("${arg.name.toCamelCase()} = $conversion")
    }

    return params.joinToString(", ")
  }

  private fun buildParameterList(args: List<InstructionArgumentNode>): String {
    val params = mutableListOf<String>()

    instruction.accounts.forEach { account ->
      val oldName = deprecation.paramMapping[account.name.toCamelCase()]
        ?: account.name.toCamelCase()
      params.add(oldName)
    }

    args.forEach { arg ->
      val oldName = deprecation.paramMapping[arg.name.toCamelCase()]
        ?: arg.name.toCamelCase()
      val conversion = when (arg.type.format) {
        "u64" -> "$oldName.toULong()"
        "u8" -> "$oldName.toUByte()"
        "u16" -> "$oldName.toUShort()"
        "u32" -> "$oldName.toUInt()"
        else -> oldName
      }
      params.add(conversion)
    }

    return params.joinToString(", ")
  }

}
