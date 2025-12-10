package net.avianlabs.solana.codegen.generator

import com.squareup.kotlinpoet.*
import net.avianlabs.solana.codegen.idl.InstructionArgumentNode
import net.avianlabs.solana.codegen.idl.InstructionNode
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class DeprecatedFunctionGenerator(
  private val instruction: InstructionNode,
  private val deprecation: DeprecationMapper.DeprecatedFunction
) {
  
  fun generate(): FunSpec {
  val nonDiscriminatorArgs = instruction.arguments.filter { 
    arg -> instruction.discriminators.none { it.name == arg.name }
  }
  
  return FunSpec.builder(deprecation.oldName)
    .addModifiers(KModifier.PUBLIC)
    .addAnnotation(buildDeprecationAnnotation())
    .apply {
    addParameters(nonDiscriminatorArgs)
    }
    .returns(ClassName("net.avianlabs.solana.domain.core", "TransactionInstruction"))
    .addStatement("return %L(%L)", 
    instruction.name.toCamelCase(),
    buildParameterList(nonDiscriminatorArgs)
    )
    .build()
  }
  
  private fun buildDeprecationAnnotation(): AnnotationSpec {
  val newParamList = buildNewParameterList()
  
  return AnnotationSpec.builder(Deprecated::class)
    .addMember("message = %S", "Use ${instruction.name.toCamelCase()} instead")
    .addMember("replaceWith = ReplaceWith(%S)", 
    "${instruction.name.toCamelCase()}($newParamList)"
    )
    .build()
  }
  
  private fun FunSpec.Builder.addParameters(args: List<InstructionArgumentNode>) {
  instruction.accounts.forEach { account ->
    val shouldSkip = account.defaultValue?.let { defaultValue ->
    (defaultValue as? JsonObject)?.let { obj ->
      val kind = obj["kind"]?.jsonPrimitive?.content
      kind == "payerValueNode" || kind == "publicKeyValueNode"
    } ?: false
    } ?: false
    
    if (!shouldSkip) {
    val oldParamName = deprecation.paramMapping[account.name.toCamelCase()] 
      ?: account.name.toCamelCase()
    addParameter(
      oldParamName,
      ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
    )
    }
  }
  
  args.forEach { arg ->
    val oldParamName = deprecation.paramMapping[arg.name.toCamelCase()] 
    ?: arg.name.toCamelCase()
    addParameter(oldParamName, LONG)
  }
  }
  
  private fun buildNewParameterList(): String {
  val params = mutableListOf<String>()
  
  instruction.accounts.forEach { account ->
    val shouldSkip = account.defaultValue?.let { defaultValue ->
    (defaultValue as? JsonObject)?.let { obj ->
      val kind = obj["kind"]?.jsonPrimitive?.content
      kind == "payerValueNode" || kind == "publicKeyValueNode"
    } ?: false
    } ?: false
    
    if (!shouldSkip) {
    val oldName = deprecation.paramMapping[account.name.toCamelCase()] 
      ?: account.name.toCamelCase()
    params.add("${account.name.toCamelCase()} = $oldName")
    }
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
    val shouldSkip = account.defaultValue?.let { defaultValue ->
    (defaultValue as? JsonObject)?.let { obj ->
      val kind = obj["kind"]?.jsonPrimitive?.content
      kind == "payerValueNode" || kind == "publicKeyValueNode"
    } ?: false
    } ?: false
    
    if (!shouldSkip) {
    val oldName = deprecation.paramMapping[account.name.toCamelCase()] 
      ?: account.name.toCamelCase()
    params.add(oldName)
    }
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
  
  private fun String.toCamelCase(): String {
  val pascal = split('_', '-')
    .joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }
  return pascal.replaceFirstChar(Char::lowercaseChar)
  }
}
