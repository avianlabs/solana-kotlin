package net.avianlabs.solana.codegen.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import net.avianlabs.solana.codegen.idl.*
import kotlinx.serialization.json.*

// Unsigned type names
private val UBYTE = ClassName("kotlin", "UByte")
private val USHORT = ClassName("kotlin", "UShort")
private val UINT = ClassName("kotlin", "UInt")
private val ULONG = ClassName("kotlin", "ULong")

class ProgramGenerator(private val program: ProgramNode) {
  
  private val packageName = "net.avianlabs.solana.domain.program"
  
  fun generate(): FileSpec {
    val fileName = program.name.toPascalCase() + "Program"
    
    return FileSpec.builder(packageName, fileName)
      .indent("  ")
      .addImport("net.avianlabs.solana.domain.core", "AccountMeta", "TransactionInstruction")
      .addImport("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
      .addImport("net.avianlabs.solana.domain.program.Program.Companion", "createTransactionInstruction")
      .addImport("okio", "Buffer")
      .addType(generateProgramObject())
      .build()
  }
  
  private fun generateProgramObject(): TypeSpec {
    val programName = program.name.toPascalCase() + "Program"
    
    return TypeSpec.objectBuilder(programName)
      .addSuperinterface(ClassName(packageName, "Program"))
      .addProperty(generateProgramIdProperty())
      .apply {
        collectSysvarConstants().forEach { (name, address) ->
          addProperty(
            PropertySpec.builder(name, ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey"))
              .addModifiers(KModifier.PUBLIC)
              .initializer("PublicKey.fromBase58(%S)", address)
              .build()
          )
        }
        
        program.accounts.forEach { account ->
          account.size?.let { size ->
            val constName = "${account.name.toScreamingSnakeCase()}_LENGTH"
            addProperty(
              PropertySpec.builder(constName, LONG)
                .addModifiers(KModifier.PUBLIC)
                .initializer("%LL", size)
                .build()
            )
          }
        }
        
        if (program.instructions.isNotEmpty()) {
          addType(generateInstructionEnum())
        }
        program.instructions.forEach { instruction ->
          addFunction(generateInstructionFunction(instruction))
          
          DeprecationMapper.getDeprecationForInstruction(instruction.name)?.let { deprecation ->
            addFunction(DeprecatedFunctionGenerator(instruction, deprecation).generate())
          }
          
          if (program.publicKey == "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA") {
            addFunction(generateInternalInstructionFunction(instruction))
          }
        }
      }
      .build()
  }
  
  private fun collectSysvarConstants(): Map<String, String> {
    val sysvars = mutableMapOf<String, String>()
    program.instructions.forEach { instruction ->
      instruction.accounts.forEach { account ->
        account.defaultValue?.let { defaultValue ->
          (defaultValue as? JsonObject)?.let { obj ->
            if (obj["kind"]?.jsonPrimitive?.content == "publicKeyValueNode") {
              val pubkey = obj["publicKey"]?.jsonPrimitive?.content
            if (pubkey?.startsWith("Sysvar") == true) {
              val name = account.name
                  .replace(Regex("([a-z])([A-Z])"), "$1_$2")
                  .replace("Sysvar", "SYSVAR")
                  .uppercase()
                sysvars[name] = pubkey
              }
            }
          }
        }
      }
    }
    return sysvars
  }
  
  private fun generateProgramIdProperty(): PropertySpec {
    return PropertySpec.builder("programId", ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey"))
      .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
      .initializer("PublicKey.fromBase58(%S)", program.publicKey)
      .build()
  }
  
  private fun generateInstructionEnum(): TypeSpec {
    return TypeSpec.enumBuilder("Instruction")
      .addModifiers(KModifier.PUBLIC)
      .primaryConstructor(
        FunSpec.constructorBuilder()
          .addParameter("index", getInstructionIndexType())
          .build()
      )
      .addProperty(
        PropertySpec.builder("index", getInstructionIndexType())
          .addModifiers(KModifier.PUBLIC)
          .initializer("index")
          .build()
      )
      .apply {
        program.instructions.forEach { instruction ->
          val discriminator = getInstructionDiscriminator(instruction)
          addEnumConstant(
            instruction.name.toPascalCase(),
            TypeSpec.anonymousClassBuilder()
              .addSuperclassConstructorParameter("${discriminator}u")
              .build()
          )
        }
      }
      .build()
  }
  
  private fun getInstructionIndexType(): TypeName {
    val firstInstruction = program.instructions.firstOrNull() ?: return UBYTE
    val discriminatorNode = firstInstruction.discriminators.firstOrNull() ?: return UBYTE
    
    val argNode = firstInstruction.arguments.find { it.name == discriminatorNode.name }
    return when (argNode?.type?.format) {
      "u8" -> UBYTE
      "u16" -> USHORT
      "u32" -> UINT
      else -> UINT
    }
  }
  
  private fun getInstructionDiscriminator(instruction: InstructionNode): Int {
    val discriminatorNode = instruction.discriminators.firstOrNull() ?: return 0
    val argNode = instruction.arguments.find { it.name == discriminatorNode.name }
    val defaultValue = argNode?.defaultValue as? JsonObject
    return defaultValue?.get("number")?.jsonPrimitive?.int ?: 0
  }
  
  private fun generateInstructionFunction(instruction: InstructionNode): FunSpec {
    val functionName = instruction.name.toCamelCase()
    val nonDiscriminatorArgs = instruction.arguments.filter { 
      arg -> instruction.discriminators.none { it.name == arg.name }
    }
    
    return FunSpec.builder(functionName)
      .addModifiers(KModifier.PUBLIC)
      .apply {
        if (instruction.docs.isNotEmpty()) {
          addKdoc(instruction.docs.joinToString("\n"))
        }
      }
      .apply {
        instruction.accounts.forEach { account ->
          val shouldSkip = account.defaultValue?.let { defaultValue ->
            (defaultValue as? JsonObject)?.let { obj ->
              val kind = obj["kind"]?.jsonPrimitive?.content
              kind == "payerValueNode" || kind == "publicKeyValueNode"
            } ?: false
          } ?: false
          
          if (!shouldSkip) {
            addParameter(
              account.name.toCamelCase(),
              ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
            )
          }
        }
        nonDiscriminatorArgs.forEach { arg ->
          addParameter(
            arg.name.toCamelCase(),
            mapTypeNodeToKotlinType(arg.type)
          )
        }
      }
      .returns(ClassName("net.avianlabs.solana.domain.core", "TransactionInstruction"))
      .addCode(generateInstructionBody(instruction, nonDiscriminatorArgs))
      .build()
  }
  
  private fun generateInstructionBody(
    instruction: InstructionNode,
    args: List<InstructionArgumentNode>
  ): CodeBlock {
    val discriminator = getInstructionDiscriminator(instruction)
    
    return CodeBlock.builder()
      .add("return createTransactionInstruction(\n")
      .indent()
      .add("programId = programId,\n")
      .add("keys = listOf(\n")
      .indent()
      .apply {
        instruction.accounts.forEach { account ->
          val isSigner = when (val signerValue = account.isSigner) {
            is JsonPrimitive -> {
              if (signerValue.isString && signerValue.content == "either") {
                true
              } else {
                signerValue.booleanOrNull ?: true
              }
            }
            else -> true
          }
          
          val (accountRef, skip) = when {
            account.defaultValue != null -> {
              (account.defaultValue as? JsonObject)?.let { obj ->
                when (obj["kind"]?.jsonPrimitive?.content) {
                  "publicKeyValueNode" -> {
                    val pubkey = obj["publicKey"]?.jsonPrimitive?.content ?: ""
                    when {
                      pubkey.startsWith("Sysvar") -> {
                        val name = account.name
                          .replace(Regex("([a-z])([A-Z])"), "$1_$2")
                          .replace("Sysvar", "SYSVAR")
                          .uppercase()
                        name to false
                      }
                      pubkey == "11111111111111111111111111111111" -> "SystemProgram.programId" to false
                      pubkey == "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA" -> "TokenProgram.programId" to false
                      pubkey.isNotEmpty() -> "PublicKey.fromBase58(\"$pubkey\")" to false
                      else -> account.name.toCamelCase() to false
                    }
                  }
                  "payerValueNode" -> null to true
                  "pdaValueNode" -> null to true
                  "identityValueNode" -> account.name.toCamelCase() to false
                  else -> account.name.toCamelCase() to false
                }
              } ?: (account.name.toCamelCase() to false)
            }
            else -> account.name.toCamelCase() to false
          }
          
          if (!skip && accountRef != null) {
            add(
              "AccountMeta(%L, isSigner = %L, isWritable = %L),\n",
              accountRef,
              isSigner,
              account.isWritable
            )
          }
        }
      }
      .unindent()
      .add("),\n")
      .add("data = Buffer()\n")
      .indent()
      .apply {
        val discriminatorArg = instruction.arguments.find { arg ->
          instruction.discriminators.any { it.name == arg.name }
        }
        when (discriminatorArg?.type?.format) {
          "u8" -> add(".writeByte(Instruction.%L.index.toInt())\n", instruction.name.toPascalCase())
          "u32" -> add(".writeIntLe(Instruction.%L.index.toInt())\n", instruction.name.toPascalCase())
          else -> add(".writeIntLe(Instruction.%L.index.toInt())\n", instruction.name.toPascalCase())
        }
        
        args.forEach { arg ->
          when (arg.type.format) {
            "u64" -> add(".writeLongLe(%L.toLong())\n", arg.name.toCamelCase())
            "i64" -> add(".writeLongLe(%L)\n", arg.name.toCamelCase())
            "u8" -> add(".writeByte(%L.toInt())\n", arg.name.toCamelCase())
            "i8" -> add(".writeByte(%L.toInt())\n", arg.name.toCamelCase())
            "u32" -> add(".writeIntLe(%L.toInt())\n", arg.name.toCamelCase())
            "i32" -> add(".writeIntLe(%L)\n", arg.name.toCamelCase())
            "u16" -> add(".writeShortLe(%L.toShort())\n", arg.name.toCamelCase())
            "i16" -> add(".writeShortLe(%L)\n", arg.name.toCamelCase())
            else -> {
              if (arg.type.kind == "publicKeyTypeNode") {
                add(".write(%L.bytes)\n", arg.name.toCamelCase())
              }
            }
          }
        }
        add(".readByteArray(),\n")
      }
      .unindent()
      .unindent()
      .add(")\n")
      .build()
  }
  
  private fun mapTypeNodeToKotlinType(typeNode: TypeNode): TypeName {
    return when (typeNode.kind) {
      "numberTypeNode" -> when (typeNode.format) {
        "u8" -> UBYTE
        "u16" -> USHORT
        "u32" -> UINT
        "u64" -> ULONG
        "i8" -> BYTE
        "i16" -> SHORT
        "i32" -> INT
        "i64" -> LONG
        else -> LONG
      }
      "publicKeyTypeNode" -> ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
      "stringTypeNode" -> STRING
      "booleanTypeNode" -> BOOLEAN
      else -> ANY
    }
  }
  
  private fun String.toPascalCase(): String {
    return split('_', '-')
      .joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }
  }
  
  private fun String.toCamelCase(): String {
    val pascal = toPascalCase()
    return pascal.replaceFirstChar(Char::lowercaseChar)
  }
  
  private fun String.toScreamingSnakeCase(): String {
    return split('_', '-')
      .joinToString("_") { it.uppercase() }
  }
  
  private fun generateInternalInstructionFunction(instruction: InstructionNode): FunSpec {
    val functionName = "create" + instruction.name.toPascalCase() + "Instruction"
    val nonDiscriminatorArgs = instruction.arguments.filter { 
      arg -> instruction.discriminators.none { it.name == arg.name }
    }
    
    return FunSpec.builder(functionName)
      .addModifiers(KModifier.INTERNAL)
      .apply {
        instruction.accounts.forEach { account ->
          val shouldSkip = account.defaultValue?.let { defaultValue ->
            (defaultValue as? JsonObject)?.let { obj ->
              val kind = obj["kind"]?.jsonPrimitive?.content
              kind == "payerValueNode" || kind == "publicKeyValueNode"
            } ?: false
          } ?: false
          
          if (!shouldSkip) {
            addParameter(
              account.name.toCamelCase(),
              ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
            )
          }
        }
        nonDiscriminatorArgs.forEach { arg ->
          addParameter(
            arg.name.toCamelCase(),
            mapTypeNodeToKotlinType(arg.type)
          )
        }
        addParameter(
          "programId",
          ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
        )
      }
      .returns(ClassName("net.avianlabs.solana.domain.core", "TransactionInstruction"))
      .addCode(generateInternalInstructionBody(instruction, nonDiscriminatorArgs))
      .build()
  }
  
  private fun generateInternalInstructionBody(
    instruction: InstructionNode,
    args: List<InstructionArgumentNode>
  ): CodeBlock {
    return CodeBlock.builder()
      .add("return createTransactionInstruction(\n")
      .indent()
      .add("programId = programId,\n")
      .add("keys = listOf(\n")
      .indent()
      .apply {
        instruction.accounts.forEach { account ->
          val isSigner = when (val signerValue = account.isSigner) {
            is JsonPrimitive -> {
              if (signerValue.isString && signerValue.content == "either") {
                true
              } else {
                signerValue.booleanOrNull ?: true
              }
            }
            else -> true
          }
          
          val (accountRef, skip) = when {
            account.defaultValue != null -> {
              (account.defaultValue as? JsonObject)?.let { obj ->
                when (obj["kind"]?.jsonPrimitive?.content) {
                  "publicKeyValueNode" -> {
                    val pubkey = obj["publicKey"]?.jsonPrimitive?.content ?: ""
                    if (pubkey.startsWith("Sysvar")) {
                      val name = account.name
                        .replace(Regex("([a-z])([A-Z])"), "$1_$2")
                        .replace("Sysvar", "SYSVAR")
                        .uppercase()
                      name to false
                    } else if (pubkey == "11111111111111111111111111111111") {
                      "SystemProgram.programId" to false
                    } else if (pubkey == "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA") {
                      "TokenProgram.programId" to false
                    } else if (pubkey.isNotEmpty()) {
                      "PublicKey.fromBase58(\"$pubkey\")" to false
                    } else {
                      account.name.toCamelCase() to false
                    }
                  }
                  "payerValueNode" -> null to true
                  "pdaValueNode" -> null to true
                  "identityValueNode" -> account.name.toCamelCase() to false
                  else -> account.name.toCamelCase() to false
                }
              } ?: (account.name.toCamelCase() to false)
            }
            else -> account.name.toCamelCase() to false
          }
          
          if (!skip && accountRef != null) {
            add(
              "AccountMeta(%L, isSigner = %L, isWritable = %L),\n",
              accountRef,
              isSigner,
              account.isWritable
            )
          }
        }
      }
      .unindent()
      .add("),\n")
      .add("data = Buffer()\n")
      .indent()
      .apply {
        val discriminatorArg = instruction.arguments.find { arg ->
          instruction.discriminators.any { it.name == arg.name }
        }
        when (discriminatorArg?.type?.format) {
          "u8" -> add(".writeByte(Instruction.%L.index.toInt())\n", instruction.name.toPascalCase())
          "u32" -> add(".writeIntLe(Instruction.%L.index.toInt())\n", instruction.name.toPascalCase())
          else -> add(".writeIntLe(Instruction.%L.index.toInt())\n", instruction.name.toPascalCase())
        }
        
        args.forEach { arg ->
          when (arg.type.format) {
            "u64" -> add(".writeLongLe(%L.toLong())\n", arg.name.toCamelCase())
            "i64" -> add(".writeLongLe(%L)\n", arg.name.toCamelCase())
            "u8" -> add(".writeByte(%L.toInt())\n", arg.name.toCamelCase())
            "i8" -> add(".writeByte(%L.toInt())\n", arg.name.toCamelCase())
            "u32" -> add(".writeIntLe(%L.toInt())\n", arg.name.toCamelCase())
            "i32" -> add(".writeIntLe(%L)\n", arg.name.toCamelCase())
            "u16" -> add(".writeShortLe(%L.toShort())\n", arg.name.toCamelCase())
            "i16" -> add(".writeShortLe(%L)\n", arg.name.toCamelCase())
            else -> {
              if (arg.type.kind == "publicKeyTypeNode") {
                add(".write(%L.bytes)\n", arg.name.toCamelCase())
              }
            }
          }
        }
        add(".readByteArray(),\n")
      }
      .unindent()
      .unindent()
      .add(")\n")
      .build()
  }
  
}
