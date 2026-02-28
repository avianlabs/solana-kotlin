package net.avianlabs.solana.codegen.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
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

        DeprecationMapper.getDeprecatedConstantsForProgram(program.name)
          .forEach { deprecatedConst ->
            val type = when (deprecatedConst.type) {
              DeprecationMapper.ConstantType.PUBLIC_KEY ->
                ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")

              DeprecationMapper.ConstantType.LONG -> LONG
            }
            addProperty(
              PropertySpec.builder(deprecatedConst.oldName, type)
                .addModifiers(KModifier.PUBLIC)
                .getter(
                  FunSpec.getterBuilder().addStatement("return %L", deprecatedConst.newName).build()
                )
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
          when (definedType.type.kind) {
            "enumTypeNode" -> {
              val hasComplexVariants = definedType.type.variants?.any { 
                it.kind == "enumStructVariantTypeNode" || it.kind == "enumTupleVariantTypeNode" 
              } ?: false
              
              if (hasComplexVariants) {
                addType(generateSealedClassEnum(definedType))
              } else {
                addType(generateDefinedEnum(definedType))
              }
            }
            "structTypeNode" -> {
              addType(generateDefinedStruct(definedType))
            }
            "fixedSizeTypeNode" -> addType(generateFixedSizeType(definedType))
          }
        }

        val numericInstructions = program.instructions.filter { hasNumericDiscriminator(it) }
        if (numericInstructions.isNotEmpty()) {
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
    val numericInstructions = program.instructions.filter { hasNumericDiscriminator(it) }
    
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
        numericInstructions.forEach { instruction ->
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

  private fun hasNumericDiscriminator(instruction: InstructionNode): Boolean {
    val discriminatorNode = instruction.discriminators.firstOrNull() ?: return false
    val argNode = instruction.arguments.find { it.name == discriminatorNode.name } ?: return false
    return argNode.type.kind == "numberTypeNode"
  }

  private fun generateDefinedEnum(definedType: DefinedTypeNode): TypeSpec {
    val enumType = definedType.type
    val sizeType = getSizeTypeFromJson(enumType.size) ?: UBYTE

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

  private fun getEnumSizeFormat(enumType: TypeNode): String {
    val size = enumType.size
    if (size is JsonObject) {
      return size["format"]?.jsonPrimitive?.content ?: "u8"
    }
    return "u8"
  }

  private fun addDiscriminatorWrite(builder: CodeBlock.Builder, format: String, discriminator: Int) {
    when (format) {
      "u16" -> builder.addStatement("buffer.writeShortLe(%L)", discriminator)
      "u32" -> builder.addStatement("buffer.writeIntLe(%L)", discriminator)
      else -> builder.addStatement("buffer.writeByte(%L)", discriminator)
    }
  }

  private fun generateSealedClassEnum(definedType: DefinedTypeNode): TypeSpec {
    val enumType = definedType.type
    val className = definedType.name.toPascalCase()
    val programClassName = program.name.toPascalCase() + "Program"
    val sizeFormat = getEnumSizeFormat(enumType)

    return TypeSpec.classBuilder(className)
      .addModifiers(KModifier.PUBLIC, KModifier.SEALED)
      .addFunction(
        FunSpec.builder("serialize")
          .addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
          .returns(ClassName("kotlin", "ByteArray"))
          .build()
      )
      .apply {
        enumType.variants?.forEachIndexed { index, variant ->
          val variantName = variant.name.toPascalCase()
          when (variant.kind) {
            "enumEmptyVariantTypeNode" -> {
              addType(
                TypeSpec.objectBuilder(variantName)
                  .superclass(ClassName(packageName, programClassName, className))
                  .addFunction(
                    FunSpec.builder("serialize")
                      .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                      .returns(ClassName("kotlin", "ByteArray"))
                      .addCode(
                        CodeBlock.builder()
                          .apply {
                            if (sizeFormat == "u8") {
                              addStatement("return byteArrayOf(%L.toByte())", index)
                            } else {
                              addStatement("val buffer = Buffer()")
                              addDiscriminatorWrite(this, sizeFormat, index)
                              addStatement("return buffer.readByteArray()")
                            }
                          }
                          .build()
                      )
                      .build()
                  )
                  .build()
              )
            }
            "enumStructVariantTypeNode" -> {
              val structType = variant.struct
              val fields = extractStructFields(structType)

              if (fields.isEmpty()) {
                addType(
                  TypeSpec.objectBuilder(variantName)
                    .superclass(ClassName(packageName, programClassName, className))
                    .addFunction(
                      FunSpec.builder("serialize")
                        .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                        .returns(ClassName("kotlin", "ByteArray"))
                        .addCode(
                          CodeBlock.builder()
                            .apply {
                              if (sizeFormat == "u8") {
                                addStatement("return byteArrayOf(%L.toByte())", index)
                              } else {
                                addStatement("val buffer = Buffer()")
                                addDiscriminatorWrite(this, sizeFormat, index)
                                addStatement("return buffer.readByteArray()")
                              }
                            }
                            .build()
                        )
                        .build()
                    )
                    .build()
                )
              } else {
                addType(
                  TypeSpec.classBuilder(variantName)
                    .addModifiers(KModifier.DATA)
                    .superclass(ClassName(packageName, programClassName, className))
                    .primaryConstructor(
                      FunSpec.constructorBuilder()
                        .apply {
                          fields.forEach { field ->
                            addParameter(field.name.toCamelCase(), mapTypeNodeToKotlinTypeSafe(field.type))
                          }
                        }
                        .build()
                    )
                    .apply {
                      fields.forEach { field ->
                        addProperty(
                          PropertySpec.builder(field.name.toCamelCase(), mapTypeNodeToKotlinTypeSafe(field.type))
                            .initializer(field.name.toCamelCase())
                            .build()
                        )
                      }
                    }
                    .addFunction(
                      FunSpec.builder("serialize")
                        .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                        .returns(ClassName("kotlin", "ByteArray"))
                        .addCode(generateStructSerializeCode(index, fields, sizeFormat))
                        .build()
                    )
                    .build()
                )
              }
            }
            "enumTupleVariantTypeNode" -> {
              val tupleType = variant.tuple
              val items = tupleType?.items ?: emptyList()

              if (items.isEmpty()) {
                addType(
                  TypeSpec.objectBuilder(variantName)
                    .superclass(ClassName(packageName, programClassName, className))
                    .addFunction(
                      FunSpec.builder("serialize")
                        .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                        .returns(ClassName("kotlin", "ByteArray"))
                        .addCode(
                          CodeBlock.builder()
                            .apply {
                              if (sizeFormat == "u8") {
                                addStatement("return byteArrayOf(%L.toByte())", index)
                              } else {
                                addStatement("val buffer = Buffer()")
                                addDiscriminatorWrite(this, sizeFormat, index)
                                addStatement("return buffer.readByteArray()")
                              }
                            }
                            .build()
                        )
                        .build()
                    )
                    .build()
                )
              } else {
                addType(
                  TypeSpec.classBuilder(variantName)
                    .addModifiers(KModifier.DATA)
                    .superclass(ClassName(packageName, programClassName, className))
                    .primaryConstructor(
                      FunSpec.constructorBuilder()
                        .apply {
                          items.forEachIndexed { idx, itemType ->
                            addParameter("value$idx", mapTypeNodeToKotlinTypeSafe(itemType))
                          }
                        }
                        .build()
                    )
                    .apply {
                      items.forEachIndexed { idx, itemType ->
                        addProperty(
                          PropertySpec.builder("value$idx", mapTypeNodeToKotlinTypeSafe(itemType))
                            .initializer("value$idx")
                            .build()
                        )
                      }
                    }
                    .addFunction(
                      FunSpec.builder("serialize")
                        .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                        .returns(ClassName("kotlin", "ByteArray"))
                        .addCode(generateTupleSerializeCode(index, items, sizeFormat))
                        .build()
                    )
                    .build()
                )
              }
            }
          }
        }
      }
      .build()
  }

  private fun generateStructSerializeCode(
    discriminator: Int,
    fields: List<StructFieldTypeNode>,
    sizeFormat: String = "u8",
  ): CodeBlock {
    return CodeBlock.builder()
      .addStatement("val buffer = Buffer()")
      .apply { addDiscriminatorWrite(this, sizeFormat, discriminator) }
      .apply {
        fields.forEach { field ->
          val paramName = field.name.toCamelCase()
          addStructFieldSerialization(paramName, field.type)
        }
      }
      .addStatement("return buffer.readByteArray()")
      .build()
  }

  private fun CodeBlock.Builder.addStructFieldSerialization(paramName: String, typeNode: TypeNode) {
    when (typeNode.kind) {
      "publicKeyTypeNode" -> addStatement("buffer.write(%L.bytes)", paramName)
      "numberTypeNode" -> when (typeNode.format) {
        "u64" -> addStatement("buffer.writeLongLe(%L.toLong())", paramName)
        "i64" -> addStatement("buffer.writeLongLe(%L)", paramName)
        "u8" -> addStatement("buffer.writeByte(%L.toInt())", paramName)
        "i8" -> addStatement("buffer.writeByte(%L.toInt())", paramName)
        "u32" -> addStatement("buffer.writeIntLe(%L.toInt())", paramName)
        "i32" -> addStatement("buffer.writeIntLe(%L)", paramName)
        "u16", "shortU16" -> addStatement("buffer.writeShortLe(%L.toInt())", paramName)
        "i16" -> addStatement("buffer.writeShortLe(%L.toInt())", paramName)
        "f32" -> addStatement("buffer.writeIntLe(%L.toRawBits())", paramName)
        "f64" -> addStatement("buffer.writeLongLe(%L.toRawBits())", paramName)
      }
      "booleanTypeNode" -> addStatement("buffer.writeByte(if (%L) 1 else 0)", paramName)
      "bytesTypeNode" -> addStatement("buffer.write(%L)", paramName)
      "stringTypeNode" -> {
        addStatement("val %LBytes = %L.encodeToByteArray()", paramName, paramName)
        addStatement("buffer.writeIntLe(%LBytes.size)", paramName)
        addStatement("buffer.write(%LBytes)", paramName)
      }
      "sizePrefixTypeNode" -> {
        val innerType = typeNode.type
        if (innerType?.kind == "stringTypeNode") {
          addStatement("val %LBytes = %L.encodeToByteArray()", paramName, paramName)
          addStatement("buffer.writeIntLe(%LBytes.size)", paramName)
          addStatement("buffer.write(%LBytes)", paramName)
        } else if (innerType != null) {
          addStructFieldSerialization(paramName, innerType)
        }
      }
      "fixedSizeTypeNode" -> {
        val innerType = typeNode.type
        if (innerType?.kind == "bytesTypeNode") {
          addStatement("buffer.write(%L.bytes)", paramName)
        } else if (innerType != null) {
          addStructFieldSerialization(paramName, innerType)
        }
      }
      "zeroableOptionTypeNode" -> {
        val innerType = typeNode.item ?: return
        val zeroSize = getTypeSize(innerType)
        beginControlFlow("if (%L != null)", paramName)
        addStructFieldSerialization(paramName, innerType)
        nextControlFlow("else")
        addStatement("buffer.write(ByteArray(%L))", zeroSize)
        endControlFlow()
      }
      "optionTypeNode" -> {
        val innerType = typeNode.item ?: return
        beginControlFlow("if (%L != null)", paramName)
        addStatement("buffer.writeByte(1)")
        addStructFieldSerialization(paramName, innerType)
        nextControlFlow("else")
        addStatement("buffer.writeByte(0)")
        endControlFlow()
      }
      "definedTypeLinkNode" -> {
        val typeName = typeNode.name ?: ""
        val definedType = program.definedTypes.find { it.name == typeName }
        when (definedType?.type?.kind) {
          "enumTypeNode" -> {
            val hasComplexVariants = definedType.type.variants?.any { 
              it.kind == "enumStructVariantTypeNode" || it.kind == "enumTupleVariantTypeNode" 
            } ?: false
            if (hasComplexVariants) {
              addStatement("buffer.write(%L.serialize())", paramName)
            } else {
              addStatement("buffer.writeByte(%L.value.toInt())", paramName)
            }
          }
          "fixedSizeTypeNode" -> addStatement("buffer.write(%L.bytes)", paramName)
          "structTypeNode" -> addStatement("buffer.write(%L.serialize())", paramName)
          else -> addStatement("buffer.writeByte(%L.value.toInt())", paramName)
        }
      }
      "amountTypeNode" -> {
        val innerType = typeNode.number ?: return
        addStructFieldSerialization(paramName, innerType)
      }
      else -> {
        addStatement("// TODO: unsupported type %L for field %L", typeNode.kind, paramName)
      }
    }
  }

  private fun generateTupleSerializeCode(
    discriminator: Int,
    items: List<TypeNode>,
    sizeFormat: String = "u8",
  ): CodeBlock {
    return CodeBlock.builder()
      .addStatement("val buffer = Buffer()")
      .apply { addDiscriminatorWrite(this, sizeFormat, discriminator) }
      .apply {
        items.forEachIndexed { idx, itemType ->
          addStructFieldSerialization("value$idx", itemType)
        }
      }
      .addStatement("return buffer.readByteArray()")
      .build()
  }

  private fun extractStructFields(structType: TypeNode?): List<StructFieldTypeNode> {
    if (structType == null) return emptyList()
    
    return when (structType.kind) {
      "structTypeNode" -> structType.fields ?: emptyList()
      "sizePrefixTypeNode" -> {
        val innerType = structType.type
        if (innerType?.kind == "structTypeNode") {
          innerType.fields ?: emptyList()
        } else {
          emptyList()
        }
      }
      else -> emptyList()
    }
  }

  private fun generateDefinedStruct(definedType: DefinedTypeNode): TypeSpec {
    val structType = definedType.type
    val fields = structType.fields ?: emptyList()
    val className = definedType.name.toPascalCase()

    return TypeSpec.classBuilder(className)
      .addModifiers(KModifier.PUBLIC, KModifier.DATA)
      .primaryConstructor(
        FunSpec.constructorBuilder()
          .apply {
            fields.forEach { field ->
              addParameter(field.name.toCamelCase(), mapTypeNodeToKotlinTypeSafe(field.type))
            }
          }
          .build()
      )
      .apply {
        fields.forEach { field ->
          addProperty(
            PropertySpec.builder(field.name.toCamelCase(), mapTypeNodeToKotlinTypeSafe(field.type))
              .initializer(field.name.toCamelCase())
              .build()
          )
        }
      }
      .addFunction(
        FunSpec.builder("serialize")
          .addModifiers(KModifier.PUBLIC)
          .returns(ClassName("kotlin", "ByteArray"))
          .addCode(
            CodeBlock.builder()
              .addStatement("val buffer = Buffer()")
              .apply {
                fields.forEach { field ->
                  addStructFieldSerialization(field.name.toCamelCase(), field.type)
                }
              }
              .addStatement("return buffer.readByteArray()")
              .build()
          )
          .build()
      )
      .build()
  }

  private fun generateFixedSizeType(definedType: DefinedTypeNode): TypeSpec {
    val className = definedType.name.toPascalCase()
    val size = (definedType.type.size as? JsonPrimitive)?.int ?: 32

    return TypeSpec.classBuilder(className)
      .addModifiers(KModifier.PUBLIC, KModifier.VALUE)
      .addAnnotation(ClassName("kotlin.jvm", "JvmInline"))
      .primaryConstructor(
        FunSpec.constructorBuilder()
          .addParameter("bytes", ClassName("kotlin", "ByteArray"))
          .build()
      )
      .addProperty(
        PropertySpec.builder("bytes", ClassName("kotlin", "ByteArray"))
          .initializer("bytes")
          .build()
      )
      .addInitializerBlock(
        CodeBlock.builder()
          .addStatement("require(bytes.size == %L) { %S }", size, "$className must be $size bytes")
          .build()
      )
      .build()
  }

  private fun mapTypeNodeToKotlinTypeSafe(typeNode: TypeNode): TypeName {
    return try {
      mapTypeNodeToKotlinType(typeNode)
    } catch (e: Exception) {
      ANY
    }
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

    val isTokenProgram = program.publicKey == "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"

    return FunSpec.builder(functionName)
      .addModifiers(KModifier.PUBLIC)
      .apply {
        if (instruction.docs.isNotEmpty()) {
          addKdoc(instruction.docs.joinToString("\n"))
        }
      }
      .instructionParameters(instruction, nonDiscriminatorArgs)
      .returns(ClassName("net.avianlabs.solana.domain.core", "TransactionInstruction"))
      .apply {
        if (isTokenProgram) {
          addCode(generateDelegationToInternal(instruction, nonDiscriminatorArgs))
        } else {
          addCode(generateInstructionBody(instruction, nonDiscriminatorArgs))
        }
      }
      .build()
  }

  private fun generateDelegationToInternal(
    instruction: InstructionNode,
    args: List<InstructionArgumentNode>
  ): CodeBlock {
    val internalFunctionName = "create" + instruction.name.toPascalCase() + "Instruction"
    val params = mutableListOf<String>()

    instruction.accounts.forEach { account ->
      val name = account.name.toCamelCase()
      params.add("$name = $name")
    }

    args.forEach { arg ->
      val name = if (arg.name.toCamelCase() == "programId") "targetProgramId" else arg.name.toCamelCase()
      params.add("$name = $name")
    }

    params.add("programId = programId")

    return CodeBlock.builder()
      .addStatement("return %L(%L)", internalFunctionName, params.joinToString(", "))
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
      val paramName = if (arg.name.toCamelCase() == "programId") "targetProgramId" else arg.name.toCamelCase()
      addParameter(paramName, mapTypeNodeToKotlinType(arg.type))
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

    when (discriminatorArg?.type?.kind) {
      "bytesTypeNode" -> {
        val defaultValue = discriminatorArg.defaultValue as? JsonObject
        val hexData = defaultValue?.get("data")?.jsonPrimitive?.content
        if (hexData != null) {
          val byteArrayLiteral = hexToByteArrayLiteral(hexData)
          add(".write($byteArrayLiteral)\n")
        }
      }
      "numberTypeNode" -> {
        when (discriminatorArg.type.format) {
          "u8" -> add(".writeByte(Instruction.%L.index.toInt())\n", instruction.name.toPascalCase())
          "u32" -> add(".writeIntLe(Instruction.%L.index.toInt())\n", instruction.name.toPascalCase())
          else -> add(".writeIntLe(Instruction.%L.index.toInt())\n", instruction.name.toPascalCase())
        }
      }
      else -> add(".writeIntLe(Instruction.%L.index.toInt())\n", instruction.name.toPascalCase())
    }

    // Write default values for omitted sub-discriminator arguments
    instruction.arguments.forEach { arg ->
      if (arg.defaultValueStrategy == "omitted" && arg != discriminatorArg && arg.defaultValue != null) {
        val defaultValue = arg.defaultValue as? JsonObject ?: return@forEach
        when (defaultValue["kind"]?.jsonPrimitive?.content) {
          "numberValueNode" -> {
            val number = defaultValue["number"]?.jsonPrimitive?.int ?: return@forEach
            when (arg.type.format) {
              "u8" -> add(".writeByte(%L)\n", number)
              "u16" -> add(".writeShortLe(%L)\n", number)
              "u32" -> add(".writeIntLe(%L)\n", number)
              "u64" -> add(".writeLongLe(%LL)\n", number)
              else -> add(".writeByte(%L)\n", number)
            }
          }
          "bytesValueNode" -> {
            val hexData = defaultValue["data"]?.jsonPrimitive?.content ?: return@forEach
            val byteArrayLiteral = hexToByteArrayLiteral(hexData)
            add(".write($byteArrayLiteral)\n")
          }
        }
      }
    }

    args.forEach { arg ->
      val paramName = if (arg.name.toCamelCase() == "programId") "targetProgramId" else arg.name.toCamelCase()
      addSerializationCode(paramName, arg.type)
    }
    add(".readByteArray(),\n")
  }

  private fun hexToByteArrayLiteral(hex: String): String {
    val hexPairs = hex.chunked(2)
    return "byteArrayOf(${hexPairs.joinToString(", ") { "0x${it}.toByte()" }})"
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
        "u16", "shortU16" -> add(".writeShortLe(%L.toInt())\n", paramName)
        "i16" -> add(".writeShortLe(%L.toInt())\n", paramName)
        "f32" -> add(".writeIntLe(%L.toRawBits())\n", paramName)
        "f64" -> add(".writeLongLe(%L.toRawBits())\n", paramName)
      }

      "publicKeyTypeNode" -> add(".write(%L.bytes)\n", paramName)
      "stringTypeNode" -> add(".writeUtf8(%L)\n", paramName)
      "booleanTypeNode" -> add(".writeByte(if (%L) 1 else 0)\n", paramName)
      "bytesTypeNode" -> add(".write(%L)\n", paramName)

      "optionTypeNode" -> {
        val innerType = typeNode.item ?: error("optionTypeNode missing item")
        val prefixFormat = getPrefixFormat(typeNode.prefix) ?: "u32"
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

      "zeroableOptionTypeNode" -> {
        val innerType = typeNode.item ?: error("zeroableOptionTypeNode missing item")
        val zeroSize = getTypeSize(innerType)
        add(".apply {\n")
        add("  if (%L != null) {\n", paramName)
        addSerializationCodeWithoutPrefix(paramName, innerType, "    ")
        add("  } else {\n")
        add("    write(ByteArray(%L))\n", zeroSize)
        add("  }\n")
        add("}\n")
      }

      "remainderOptionTypeNode" -> {
        val innerType = typeNode.item ?: error("remainderOptionTypeNode missing item")
        add(".apply {\n")
        add("  if (%L != null) {\n", paramName)
        add("    ").addSerializationCode(paramName, innerType)
        add("  }\n")
        add("}\n")
      }

      "sizePrefixTypeNode" -> {
        val innerType = typeNode.type ?: error("sizePrefixTypeNode missing type")
        val prefixFormat = getPrefixFormat(typeNode.prefix) ?: "u64"
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
        } else {
          addSerializationCode(paramName, innerType)
        }
      }

      "fixedSizeTypeNode" -> {
        val innerType = typeNode.type ?: error("fixedSizeTypeNode missing type")
        addSerializationCode(paramName, innerType)
      }

      "amountTypeNode" -> {
        val innerType = typeNode.number ?: error("amountTypeNode missing number")
        addSerializationCode(paramName, innerType)
      }

      "tupleTypeNode" -> {
        val items = typeNode.items ?: error("tupleTypeNode missing items")
        when (items.size) {
          2 -> {
            addSerializationCode("$paramName.first", items[0])
            addSerializationCode("$paramName.second", items[1])
          }
          3 -> {
            addSerializationCode("$paramName.first", items[0])
            addSerializationCode("$paramName.second", items[1])
            addSerializationCode("$paramName.third", items[2])
          }
          else -> error("Tuple with ${items.size} items not supported")
        }
      }

      "mapTypeNode" -> {
        val keyType = typeNode.key ?: error("mapTypeNode missing key")
        val valueType = typeNode.value ?: error("mapTypeNode missing value")
        add(".apply {\n")
        add("  writeIntLe(%L.size)\n", paramName)
        add("  %L.forEach { (k, v) ->\n", paramName)
        add("    ").addSerializationCode("k", keyType)
        add("    ").addSerializationCode("v", valueType)
        add("  }\n")
        add("}\n")
      }

      "arrayTypeNode" -> {
        val itemType = typeNode.item ?: error("arrayTypeNode missing item")
        add(".apply {\n")
        add("  %L.forEach { item ->\n", paramName)
        addSerializationCodeWithoutPrefix("item", itemType, "    ")
        add("  }\n")
        add("}\n")
      }

      "definedTypeLinkNode" -> {
        val typeName = typeNode.name ?: ""
        val definedType = program.definedTypes.find { it.name == typeName }
        when (definedType?.type?.kind) {
          "enumTypeNode" -> {
            val hasComplexVariants = definedType.type.variants?.any { 
              it.kind == "enumStructVariantTypeNode" || it.kind == "enumTupleVariantTypeNode" 
            } ?: false
            if (hasComplexVariants) {
              add(".write(%L.serialize())\n", paramName)
            } else {
              add(".writeByte(%L.value.toInt())\n", paramName)
            }
          }
          "fixedSizeTypeNode" -> add(".write(%L.bytes)\n", paramName)
          "structTypeNode" -> add(".write(%L.serialize())\n", paramName)
          else -> add(".writeByte(%L.value.toInt())\n", paramName)
        }
      }

      "hiddenPrefixTypeNode", "preOffsetTypeNode" -> {
        val innerType = typeNode.type ?: error("${typeNode.kind} missing type")
        addSerializationCode(paramName, innerType)
      }
    }
  }

  private fun getTypeSize(typeNode: TypeNode): Int {
    return when (typeNode.kind) {
      "publicKeyTypeNode" -> 32
      "numberTypeNode" -> when (typeNode.format) {
        "u8", "i8" -> 1
        "u16", "i16" -> 2
        "u32", "i32", "f32" -> 4
        "u64", "i64", "f64" -> 8
        else -> 8
      }
      "booleanTypeNode" -> 1
      else -> 32 // default to pubkey size for zeroable options
    }
  }

  private fun CodeBlock.Builder.addSerializationCodeWithoutPrefix(paramName: String, typeNode: TypeNode, indent: String = "") {
    when (typeNode.kind) {
      "publicKeyTypeNode" -> add("${indent}write(%L.bytes)\n", paramName)
      "numberTypeNode" -> when (typeNode.format) {
        "u64" -> add("${indent}writeLongLe(%L.toLong())\n", paramName)
        "i64" -> add("${indent}writeLongLe(%L)\n", paramName)
        "u8" -> add("${indent}writeByte(%L.toInt())\n", paramName)
        "i8" -> add("${indent}writeByte(%L.toInt())\n", paramName)
        "u32" -> add("${indent}writeIntLe(%L.toInt())\n", paramName)
        "i32" -> add("${indent}writeIntLe(%L)\n", paramName)
        "u16", "shortU16" -> add("${indent}writeShortLe(%L.toInt())\n", paramName)
        "i16" -> add("${indent}writeShortLe(%L.toInt())\n", paramName)
        "f32" -> add("${indent}writeIntLe(%L.toRawBits())\n", paramName)
        "f64" -> add("${indent}writeLongLe(%L.toRawBits())\n", paramName)
      }
      "bytesTypeNode" -> add("${indent}write(%L)\n", paramName)
      "definedTypeLinkNode" -> {
        val typeName = typeNode.name ?: ""
        val definedType = program.definedTypes.find { it.name == typeName }
        when (definedType?.type?.kind) {
          "enumTypeNode" -> {
            val hasComplexVariants = definedType.type.variants?.any { 
              it.kind == "enumStructVariantTypeNode" || it.kind == "enumTupleVariantTypeNode" 
            } ?: false
            if (hasComplexVariants) {
              add("${indent}write(%L.serialize())\n", paramName)
            } else {
              add("${indent}writeByte(%L.value.toInt())\n", paramName)
            }
          }
          "fixedSizeTypeNode" -> add("${indent}write(%L.bytes)\n", paramName)
          else -> add("${indent}writeByte(%L.value.toInt())\n", paramName)
        }
      }
      else -> add("${indent}write(%L.serialize())\n", paramName)
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
        "f32" -> FLOAT
        "f64" -> DOUBLE
        "shortU16" -> USHORT
        else -> error("Unsupported number format: ${typeNode.format}")
      }

      "publicKeyTypeNode" -> ClassName("net.avianlabs.solana.tweetnacl.ed25519", "PublicKey")
      "stringTypeNode" -> STRING
      "booleanTypeNode" -> BOOLEAN
      "bytesTypeNode" -> ClassName("kotlin", "ByteArray")

      "optionTypeNode" -> {
        val innerType = typeNode.item ?: error("optionTypeNode missing item")
        mapTypeNodeToKotlinType(innerType).copy(nullable = true)
      }

      "zeroableOptionTypeNode" -> {
        val innerType = typeNode.item ?: error("zeroableOptionTypeNode missing item")
        mapTypeNodeToKotlinType(innerType).copy(nullable = true)
      }

      "remainderOptionTypeNode" -> {
        val innerType = typeNode.item ?: error("remainderOptionTypeNode missing item")
        mapTypeNodeToKotlinType(innerType).copy(nullable = true)
      }

      "sizePrefixTypeNode" -> {
        val innerType = typeNode.type ?: error("sizePrefixTypeNode missing type")
        mapTypeNodeToKotlinType(innerType)
      }

      "fixedSizeTypeNode" -> {
        val innerType = typeNode.type ?: error("fixedSizeTypeNode missing type")
        mapTypeNodeToKotlinType(innerType)
      }

      "amountTypeNode" -> {
        val innerType = typeNode.number ?: error("amountTypeNode missing number")
        mapTypeNodeToKotlinType(innerType)
      }

      "tupleTypeNode" -> {
        val items = typeNode.items ?: error("tupleTypeNode missing items")
        when (items.size) {
          2 -> ClassName("kotlin", "Pair").parameterizedBy(
            mapTypeNodeToKotlinType(items[0]),
            mapTypeNodeToKotlinType(items[1])
          )
          3 -> ClassName("kotlin", "Triple").parameterizedBy(
            mapTypeNodeToKotlinType(items[0]),
            mapTypeNodeToKotlinType(items[1]),
            mapTypeNodeToKotlinType(items[2])
          )
          else -> ClassName("kotlin", "List").parameterizedBy(ANY)
        }
      }

      "mapTypeNode" -> {
        val keyType = typeNode.key ?: error("mapTypeNode missing key")
        val valueType = typeNode.value ?: error("mapTypeNode missing value")
        ClassName("kotlin.collections", "Map").parameterizedBy(
          mapTypeNodeToKotlinType(keyType),
          mapTypeNodeToKotlinType(valueType)
        )
      }

      "arrayTypeNode" -> {
        val itemType = typeNode.item ?: error("arrayTypeNode missing item")
        ClassName("kotlin.collections", "List").parameterizedBy(mapTypeNodeToKotlinType(itemType))
      }

      "definedTypeLinkNode" -> {
        val typeName = typeNode.name ?: error("definedTypeLinkNode missing name")
        ClassName(packageName, program.name.toPascalCase() + "Program", typeName.toPascalCase())
      }

      "hiddenPrefixTypeNode", "preOffsetTypeNode" -> {
        val innerType = typeNode.type ?: error("${typeNode.kind} missing type")
        mapTypeNodeToKotlinType(innerType)
      }

      "structTypeNode" -> ANY

      else -> error("Unsupported type node kind: ${typeNode.kind}")
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

  private fun getPrefixFormat(prefix: JsonElement?): String? {
    if (prefix == null) return null
    return when (prefix) {
      is JsonObject -> prefix["format"]?.jsonPrimitive?.content
      else -> null
    }
  }

  private fun getSizeTypeFromJson(size: JsonElement?): TypeName? {
    if (size == null) return null
    return when (size) {
      is JsonObject -> {
        val format = size["format"]?.jsonPrimitive?.content
        when (format) {
          "u8" -> UBYTE
          "u16" -> USHORT
          "u32" -> UINT
          "u64" -> ULONG
          else -> null
        }
      }

      else -> null
    }
  }
}
