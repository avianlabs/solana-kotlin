package net.avianlabs.solana.codegen

import kotlinx.serialization.json.Json
import net.avianlabs.solana.codegen.generator.ProgramGenerator
import net.avianlabs.solana.codegen.generator.SharedInterfaceConfig
import net.avianlabs.solana.codegen.generator.hasUnsupportedTypes
import net.avianlabs.solana.codegen.generator.signaturesCompatible
import net.avianlabs.solana.codegen.generator.toPascalCase
import net.avianlabs.solana.codegen.idl.EnumVariantTypeNode
import net.avianlabs.solana.codegen.idl.InstructionNode
import net.avianlabs.solana.codegen.idl.ProgramNode
import net.avianlabs.solana.codegen.idl.RootNode
import java.io.File

val json = Json {
  ignoreUnknownKeys = true
}

data class BaseInterfaceConfig(
  val sealedClassName: String,
  val baseIdlFile: String,
  val baseProgramKey: String,
  val implementingProgramKeys: Set<String>,
)

private val baseInterfaceConfigs = listOf(
  BaseInterfaceConfig(
    sealedClassName = "TokenProgram",
    baseIdlFile = "token.json",
    baseProgramKey = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA",
    implementingProgramKeys = setOf(
      "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA",  // TokenProgram
      "TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb",  // Token2022Program
    ),
  ),
)

fun main() {
  val idlDir = File("codegen/idl")
  val outputDir = File("solana-kotlin/src/commonMain/generated")

  require(idlDir.exists()) { "IDL directory not found: ${idlDir.absolutePath}" }
  require(outputDir.exists()) { "Output directory not found: ${outputDir.absolutePath}" }

  println("Reading IDL files from: ${idlDir.absolutePath}")
  println("Writing generated code to: ${outputDir.absolutePath}")

  val idlFiles = idlDir.listFiles { file -> file.extension == "json" }
    ?.sortedBy { it.name }
    ?: error("Failed to list IDL files")

  // Parse all IDLs first (use LinkedHashMap to preserve alphabetical order)
  val parsedIdls = idlFiles.associate { idlFile ->
    val rootNode = json.decodeFromString<RootNode>(idlFile.readText())
    idlFile.name to rootNode
  }

  // Build shared configs for sealed class generation
  // Key: program publicKey -> SharedInterfaceConfig
  val sharedConfigs = mutableMapOf<String, SharedInterfaceConfig>()

  baseInterfaceConfigs.forEach { config ->
    val baseRoot = parsedIdls[config.baseIdlFile]
      ?: error("Base IDL file not found: ${config.baseIdlFile}")
    val baseProgram = baseRoot.program

    // Only treat an instruction as "shared" if every implementing program has it
    // with a compatible signature. Instructions that exist only in the base program,
    // or whose signatures have diverged across implementations, become regular
    // (non-overriding) methods on each program rather than abstract on the parent.
    val sharedInstructionNames = computeSharedInstructionNames(
      baseProgram = baseProgram,
      implementingProgramKeys = config.implementingProgramKeys,
      parsedIdls = parsedIdls,
    )

    // Collect defined type names referenced in base instruction parameters
    val sharedDefinedTypeNames = collectReferencedDefinedTypes(baseProgram)

    // Collect merged defined type variants from ALL implementing programs
    val mergedDefinedTypes = collectMergedDefinedTypes(
      sharedDefinedTypeNames = sharedDefinedTypeNames,
      implementingProgramKeys = config.implementingProgramKeys,
      parsedIdls = parsedIdls,
    )

    val sharedConfig = SharedInterfaceConfig(
      sealedClassName = config.sealedClassName,
      sharedInstructionNames = sharedInstructionNames,
      sharedDefinedTypeNames = sharedDefinedTypeNames,
      baseProgramKey = config.baseProgramKey,
      mergedDefinedTypes = mergedDefinedTypes,
    )

    // Map each implementing program's public key to this config
    config.implementingProgramKeys.forEach { pubkey ->
      sharedConfigs[pubkey] = sharedConfig
    }

    // Delete the old shared interface file if it exists
    val oldInterfaceFile = outputDir.resolve(
      "net/avianlabs/solana/domain/program/TokenProgramBase.kt"
    )
    if (oldInterfaceFile.exists()) {
      oldInterfaceFile.delete()
      println("  ✓ Deleted old shared interface TokenProgramBase.kt")
    }
  }

  // Generate programs
  parsedIdls.forEach { (idlFileName, rootNode) ->
    println("\nProcessing: $idlFileName")

    fun configForProgram(program: ProgramNode) = sharedConfigs[program.publicKey]

    generateProgram(rootNode.program, outputDir, idlFileName, configForProgram(rootNode.program))
    rootNode.additionalPrograms.forEach { generateProgram(it, outputDir, idlFileName, configForProgram(it)) }

    println("  ✓ Generated ${rootNode.program.name}")
    if (rootNode.additionalPrograms.isNotEmpty()) {
      println("  ✓ Generated ${rootNode.additionalPrograms.size} additional program(s)")
    }
  }

  println("\n✓ Code generation complete!")
}

private fun computeSharedInstructionNames(
  baseProgram: ProgramNode,
  implementingProgramKeys: Set<String>,
  parsedIdls: Map<String, RootNode>,
): Set<String> {
  val implementingPrograms = parsedIdls.values
    .flatMap { listOf(it.program) + it.additionalPrograms }
    .filter { it.publicKey in implementingProgramKeys }
  val nonBasePrograms = implementingPrograms.filter { it.publicKey != baseProgram.publicKey }

  return baseProgram.instructions
    .filter { instruction: InstructionNode ->
      if (instruction.hasUnsupportedTypes()) return@filter false
      nonBasePrograms.all { other ->
        val match = other.instructions.find { it.name == instruction.name } ?: return@all false
        !match.hasUnsupportedTypes() && signaturesCompatible(instruction, match)
      }
    }
    .map { it.name }
    .toSet()
}

private fun collectMergedDefinedTypes(
  sharedDefinedTypeNames: Set<String>,
  implementingProgramKeys: Set<String>,
  parsedIdls: Map<String, RootNode>,
): Map<String, List<EnumVariantTypeNode>> {
  val merged = mutableMapOf<String, MutableList<EnumVariantTypeNode>>()

  // Initialize with empty lists for each shared type
  sharedDefinedTypeNames.forEach { typeName ->
    merged[typeName] = mutableListOf()
  }

  // Collect all programs that implement this shared interface
  val allPrograms = parsedIdls.values.flatMap { root ->
    listOf(root.program) + root.additionalPrograms
  }.filter { it.publicKey in implementingProgramKeys }

  // For each shared defined type, collect ALL variants from ALL implementing programs
  // Use the longest variant list (the one with the most entries wins, since they share a prefix)
  sharedDefinedTypeNames.forEach { typeName ->
    var longestVariants: List<EnumVariantTypeNode> = emptyList()
    allPrograms.forEach { program ->
      val definedType = program.definedTypes.find { it.name == typeName }
      if (definedType != null && definedType.type.kind == "enumTypeNode") {
        val variants = definedType.type.variants ?: emptyList()
        if (variants.size > longestVariants.size) {
          longestVariants = variants
        }
      }
    }
    merged[typeName] = longestVariants.toMutableList()
  }

  return merged
}

private fun collectReferencedDefinedTypes(program: ProgramNode): Set<String> {
  val referencedTypes = mutableSetOf<String>()
  program.instructions.forEach { instruction ->
    instruction.arguments.forEach { arg ->
      collectDefinedTypeLinkNames(arg.type, referencedTypes)
    }
  }
  return referencedTypes
}

private fun collectDefinedTypeLinkNames(
  typeNode: net.avianlabs.solana.codegen.idl.TypeNode,
  result: MutableSet<String>,
) {
  when (typeNode.kind) {
    "definedTypeLinkNode" -> typeNode.name?.let { result.add(it) }
    "optionTypeNode", "zeroableOptionTypeNode", "remainderOptionTypeNode" ->
      typeNode.item?.let { collectDefinedTypeLinkNames(it, result) }
    "sizePrefixTypeNode", "fixedSizeTypeNode", "hiddenPrefixTypeNode", "preOffsetTypeNode" ->
      typeNode.type?.let { collectDefinedTypeLinkNames(it, result) }
    "amountTypeNode" -> typeNode.number?.let { collectDefinedTypeLinkNames(it, result) }
    "tupleTypeNode" -> typeNode.items?.forEach { collectDefinedTypeLinkNames(it, result) }
    "mapTypeNode" -> {
      typeNode.key?.let { collectDefinedTypeLinkNames(it, result) }
      typeNode.value?.let { collectDefinedTypeLinkNames(it, result) }
    }
    "arrayTypeNode" -> typeNode.item?.let { collectDefinedTypeLinkNames(it, result) }
  }
}

private fun generateProgram(
  program: ProgramNode,
  outputDir: File,
  idlFileName: String,
  sharedConfig: SharedInterfaceConfig?,
) {
  val generator = ProgramGenerator(program, sharedConfig)
  val fileSpec = generator.generate()
  writeGeneratedFile(
    fileSpec = fileSpec,
    outputDir = outputDir,
    fileName = program.name.toPascalCase() + "Program",
    idlFileName = idlFileName,
  )
}

private fun writeGeneratedFile(
  fileSpec: com.squareup.kotlinpoet.FileSpec,
  outputDir: File,
  fileName: String,
  idlFileName: String,
) {
  val outputFile =
    outputDir.resolve("net/avianlabs/solana/domain/program/$fileName.kt")
  outputFile.parentFile.mkdirs()

  val header = """// This code was AUTOGENERATED using Codama IDLs.
// Please DO NOT EDIT THIS FILE manually.
// Instead, update the IDL and regenerate using:
// ./gradlew :codegen:generateSolanaCode
//
// IDL source: codegen/idl/$idlFileName

"""

  val generatedCode = StringBuilder()
  fileSpec.writeTo(generatedCode)

  outputFile.writeText(header + generatedCode.toString())
}
