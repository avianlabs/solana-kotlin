package net.avianlabs.solana.codegen.generator

import net.avianlabs.solana.codegen.idl.EnumVariantTypeNode

data class SharedInterfaceConfig(
  val sealedClassName: String,
  val sharedInstructionNames: Set<String>,
  val sharedDefinedTypeNames: Set<String>,
  val baseProgramKey: String,
  val mergedDefinedTypes: Map<String, List<EnumVariantTypeNode>>,
) {
  fun isSharedInstruction(name: String): Boolean = name in sharedInstructionNames
  fun isSharedDefinedType(name: String): Boolean = name in sharedDefinedTypeNames
  fun isBaseProgram(publicKey: String): Boolean = publicKey == baseProgramKey
}
