package net.avianlabs.solana.codegen.generator

import com.squareup.kotlinpoet.*
import kotlinx.serialization.json.*
import net.avianlabs.solana.codegen.idl.*

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
      .addImport(
        "net.avianlabs.solana.domain.program.Program.Companion",
        "createTransactionInstruction"
      )
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
            PropertySpec.builder(
              name,
              ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
            )
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

        DeprecationMapper.getDeprecatedConstantsForProgram(program.name).forEach { deprecatedConst ->
          val type = when (deprecatedConst.type) {
            DeprecationMapper.ConstantType.PUBLIC_KEY ->
              ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
            DeprecationMapper.ConstantType.LONG -> LONG
          }
          addProperty(
            PropertySpec.builder(deprecatedConst.oldName, type)
              .addModifiers(KModifier.PUBLIC)
              .getter(FunSpec.getterBuilder().addStatement("return %L", deprecatedConst.newName).build())
              .addAnnotation(
                AnnotationSpec.builder(Deprecated::class)
                  .addMember("message = %S", "Use ${deprecatedConst.newName} instead")
                  .addMember("replaceWith = ReplaceWith(%S)", deprecatedConst.newName)
                  .build()
              )
              .build()
          )
        }

        program.definedTypes.forEach { definedType ->
          if (definedType.type.kind == "enumTypeNode") {
            addType(generateDefinedEnum(definedType))
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
    return PropertySpec.builder(
      "programId",
      ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
    )
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

  private fun generateDefinedEnum(definedType: DefinedTypeNode): TypeSpec {
    val enumType = definedType.type
    val sizeType = enumType.size?.let { mapTypeNodeToKotlinType(it) } ?: UBYTE

    return TypeSpec.enumBuilder(definedType.name.toPascalCase())
      .addModifiers(KModifier.PUBLIC)
      .primaryConstructor(
        FunSpec.constructorBuilder()
          .addParameter("value", sizeType)
          .build()
      )
      .addProperty(
        PropertySpec.builder("value", sizeType)
          .addModifiers(KModifier.PUBLIC)
          .initializer("value")
          .build()
      )
      .apply {
        enumType.variants?.forEachIndexed { index, variant ->
          addEnumConstant(
            variant.name.toPascalCase(),
            TypeSpec.anonymousClassBuilder()
              .addSuperclassConstructorParameter("${index}u")
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
    val nonDiscriminatorArgs = instruction.arguments.filter { arg ->
      instruction.discriminators.none { it.name == arg.name }
    }

    return FunSpec.builder(functionName)
      .addModifiers(KModifier.PUBLIC)
      .apply {
        if (instruction.docs.isNotEmpty()) {
          addKdoc(instruction.docs.joinToString("\n"))
        }
      }
      .instructionParameters(instruction, nonDiscriminatorArgs)
      .returns(ClassName("net.avianlabs.solana.domain.core", "TransactionInstruction"))
      .addCode(generateInstructionBody(instruction, nonDiscriminatorArgs))
      .build()
  }

  private fun FunSpec.Builder.instructionParameters(
    instruction: InstructionNode,
    nonDiscriminatorArgs: List<InstructionArgumentNode>
  ) = apply {
    instruction.accounts.filter { !it.hasPublicKeyDefault() }.forEach { account ->
      addParameter(
        account.name.toCamelCase(),
        ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
      )
    }
    nonDiscriminatorArgs.forEach { arg ->
      addParameter(arg.name.toCamelCase(), mapTypeNodeToKotlinType(arg.type))
    }
    instruction.accounts.filter { it.hasPublicKeyDefault() }.forEach { account ->
      val defaultExpr = getDefaultExpression(account)
      addParameter(
        ParameterSpec.builder(
          account.name.toCamelCase(),
          ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
        ).defaultValue(defaultExpr).build()
      )
    }
  }

  private fun getDefaultExpression(account: InstructionAccountNode): String {
    val pubkey = account.getPublicKeyDefault() ?: return "TODO()"
    return when {
      pubkey.startsWith("Sysvar") -> {
        val name = account.name
          .replace(Regex("([a-z])([A-Z])"), "$1_$2")
          .replace("Sysvar", "SYSVAR")
          .uppercase()
        name
      }
      pubkey == "11111111111111111111111111111111" -> "SystemProgram.programId"
      pubkey == "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA" -> "TokenProgram.programId"
      pubkey.isNotEmpty() -> "PublicKey.fromBase58(\"$pubkey\")"
      else -> "TODO()"
    }
  }

  private fun generateInstructionBody(
    instruction: InstructionNode,
    args: List<InstructionArgumentNode>
  ): CodeBlock = CodeBlock.builder()
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

        val accountRef = account.name.toCamelCase()
        add(
          "AccountMeta(%L, isSigner = %L, isWritable = %L),\n",
          accountRef,
          isSigner,
          account.isWritable
        )
      }
    }
    .unindent()
    .add("),\n")
    .add("data = Buffer()\n")
    .indent()
    .writeInstructions(instruction, args)
    .unindent()
    .unindent()
    .add(")\n")
    .build()

  private fun CodeBlock.Builder.writeInstructions(
    instruction: InstructionNode,
    args: List<InstructionArgumentNode>
  ) = apply {
    val discriminatorArg = instruction.arguments.find { arg ->
      instruction.discriminators.any { it.name == arg.name }
    }
    when (discriminatorArg?.type?.format) {
      "u8" -> add(".writeByte(Instruction.%L.index.toInt())\n", instruction.name.toPascalCase())
      "u32" -> add(
        ".writeIntLe(Instruction.%L.index.toInt())\n",
        instruction.name.toPascalCase()
      )

      else -> add(
        ".writeIntLe(Instruction.%L.index.toInt())\n",
        instruction.name.toPascalCase()
      )
    }

    args.forEach { arg ->
      addSerializationCode(arg.name.toCamelCase(), arg.type)
    }
    add(".readByteArray(),\n")
  }

  private fun CodeBlock.Builder.addSerializationCode(paramName: String, typeNode: TypeNode) {
    when (typeNode.kind) {
      "numberTypeNode" -> when (typeNode.format) {
        "u64" -> add(".writeLongLe(%L.toLong())\n", paramName)
        "i64" -> add(".writeLongLe(%L)\n", paramName)
        "u8" -> add(".writeByte(%L.toInt())\n", paramName)
        "i8" -> add(".writeByte(%L.toInt())\n", paramName)
        "u32" -> add(".writeIntLe(%L.toInt())\n", paramName)
        "i32" -> add(".writeIntLe(%L)\n", paramName)
        "u16" -> add(".writeShortLe(%L.toInt())\n", paramName)
        "i16" -> add(".writeShortLe(%L.toInt())\n", paramName)
      }

      "publicKeyTypeNode" -> add(".write(%L.bytes)\n", paramName)
      "stringTypeNode" -> add(".writeUtf8(%L)\n", paramName)
      "booleanTypeNode" -> add(".writeByte(if (%L) 1 else 0)\n", paramName)
      "optionTypeNode" -> {
        val innerType = typeNode.item ?: error("optionTypeNode missing item")
        val prefixFormat = typeNode.prefix?.format ?: "u32"
        add(".apply {\n")
        add("  if (%L != null) {\n", paramName)
        when (prefixFormat) {
          "u8" -> add("    writeByte(1)\n")
          "u16" -> add("    writeShortLe(1)\n")
          "u32" -> add("    writeIntLe(1)\n")
          else -> add("    writeIntLe(1)\n")
        }
        add("    ").addSerializationCode(paramName, innerType)
        add("  } else {\n")
        when (prefixFormat) {
          "u8" -> add("    writeByte(0)\n")
          "u16" -> add("    writeShortLe(0)\n")
          "u32" -> add("    writeIntLe(0)\n")
          else -> add("    writeIntLe(0)\n")
        }
        add("  }\n")
        add("}\n")
      }

      "sizePrefixTypeNode" -> {
        val innerType = typeNode.type ?: error("sizePrefixTypeNode missing type")
        val prefixFormat = typeNode.prefix?.format ?: "u64"
        if (innerType.kind == "stringTypeNode") {
          add(".apply {\n")
          add("  val bytes = %L.encodeToByteArray()\n", paramName)
          when (prefixFormat) {
            "u8" -> add("  writeByte(bytes.size)\n")
            "u16" -> add("  writeShortLe(bytes.size)\n")
            "u32" -> add("  writeIntLe(bytes.size)\n")
            "u64" -> add("  writeLongLe(bytes.size.toLong())\n")
            else -> add("  writeLongLe(bytes.size.toLong())\n")
          }
          add("  write(bytes)\n")
          add("}\n")
        }
      }

      "definedTypeLinkNode" -> add(".writeByte(%L.value.toInt())\n", paramName)
    }
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
        else -> error("Unsupported number format: ${typeNode.format}")
      }

      "publicKeyTypeNode" -> ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
      "stringTypeNode" -> STRING
      "booleanTypeNode" -> BOOLEAN
      "optionTypeNode" -> {
        val innerType = typeNode.item ?: error("optionTypeNode missing item")
        mapTypeNodeToKotlinType(innerType).copy(nullable = true)
      }

      "sizePrefixTypeNode" -> {
        val innerType = typeNode.type ?: error("sizePrefixTypeNode missing type")
        mapTypeNodeToKotlinType(innerType)
      }

      "definedTypeLinkNode" -> {
        val typeName = typeNode.name ?: error("definedTypeLinkNode missing name")
        ClassName(packageName, program.name.toPascalCase() + "Program", typeName.toPascalCase())
      }

      else -> error("Unsupported type node kind: ${typeNode.kind}")
    }
  }

  private fun mapTypeNodeToKotlinTypeSafe(typeNode: TypeNode): TypeName {
    return try {
      mapTypeNodeToKotlinType(typeNode)
    } catch (e: IllegalStateException) {
      System.err.println("Warning: unmapped type ${typeNode.kind}, falling back to Any")
      ANY
    }
  }

  private fun String.toScreamingSnakeCase(): String {
    return split('_', '-')
      .joinToString("_") { it.uppercase() }
  }

  private fun generateInternalInstructionFunction(instruction: InstructionNode): FunSpec {
    val functionName = "create" + instruction.name.toPascalCase() + "Instruction"
    val nonDiscriminatorArgs = instruction.arguments.filter { arg ->
      instruction.discriminators.none { it.name == arg.name }
    }

    return FunSpec.builder(functionName)
      .addModifiers(KModifier.INTERNAL)
      .instructionParameters(instruction, nonDiscriminatorArgs).apply {
        addParameter(
          "programId",
          ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
        )
      }
      .returns(ClassName("net.avianlabs.solana.domain.core", "TransactionInstruction"))
      .addCode(generateInstructionBody(instruction, nonDiscriminatorArgs))
      .build()
  }
}
