package net.avianlabs.solana.codegen

import kotlinx.serialization.json.Json
import net.avianlabs.solana.codegen.generator.ProgramGenerator
import net.avianlabs.solana.codegen.idl.RootNode
import okio.FileSystem
import okio.Path.Companion.toPath
import java.io.File

val json = Json {
  ignoreUnknownKeys = true
  isLenient = true
}

fun main() {
  val idlDir = File("codegen/idl")
  val outputDir = File("solana-kotlin/src/commonMain/kotlin")
  
  require(idlDir.exists()) { "IDL directory not found: ${idlDir.absolutePath}" }
  require(outputDir.exists()) { "Output directory not found: ${outputDir.absolutePath}" }
  
  println("Reading IDL files from: ${idlDir.absolutePath}")
  println("Writing generated code to: ${outputDir.absolutePath}")
  
  val idlFiles = idlDir.listFiles { file -> file.extension == "json" }
    ?: error("Failed to list IDL files")
  
  idlFiles.forEach { idlFile ->
    println("\nProcessing: ${idlFile.name}")
    try {
      val idlText = idlFile.readText()
      val rootNode = json.decodeFromString<RootNode>(idlText)
      
      // Generate main program
      generateProgram(rootNode.program, outputDir)
      
      // Generate additional programs (like ATA in token.json)
      rootNode.additionalPrograms.forEach { additionalProgram ->
        generateProgram(additionalProgram, outputDir)
      }
      
      println("  ✓ Generated ${rootNode.program.name}")
      if (rootNode.additionalPrograms.isNotEmpty()) {
        println("  ✓ Generated ${rootNode.additionalPrograms.size} additional program(s)")
      }
    } catch (e: Exception) {
      println("  ✗ Error: ${e.message}")
      e.printStackTrace()
    }
  }
  
  println("\n✓ Code generation complete!")
}

fun generateProgram(program: net.avianlabs.solana.codegen.idl.ProgramNode, outputDir: File) {
  val generator = ProgramGenerator(program)
  val fileSpec = generator.generate()
  
  fileSpec.writeTo(outputDir)
}
