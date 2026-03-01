package net.avianlabs.solana.codegen.idl

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RootNode(
  val kind: String = "rootNode",
  val program: ProgramNode,
  val additionalPrograms: List<ProgramNode> = emptyList(),
  val standard: String = "codama",
  val version: String
)

@Serializable
data class ProgramNode(
  val kind: String = "programNode",
  val name: String,
  val publicKey: String,
  val version: String = "0.0.0",
  val origin: String = "shank",
  val prefix: String = "",
  val accounts: List<AccountNode> = emptyList(),
  val instructions: List<InstructionNode> = emptyList(),
  val definedTypes: List<DefinedTypeNode> = emptyList(),
  val errors: List<ErrorNode> = emptyList(),
  val pdas: List<PdaNode> = emptyList()
)

@Serializable
data class InstructionNode(
  val kind: String = "instructionNode",
  val name: String,
  val idlName: String? = null,
  val docs: List<String> = emptyList(),
  val accounts: List<InstructionAccountNode> = emptyList(),
  val arguments: List<InstructionArgumentNode> = emptyList(),
  val discriminators: List<DiscriminatorNode> = emptyList(),
  val byteDeltas: List<JsonElement> = emptyList(),
  val remainingAccounts: List<JsonElement> = emptyList(),
  val optionalAccountStrategy: String = "omitted"
)

@Serializable
data class InstructionAccountNode(
  val kind: String = "instructionAccountNode",
  val name: String,
  val isWritable: Boolean,
  val isSigner: JsonElement, // can be boolean or "either"
  val isOptional: Boolean,
  val docs: List<String> = emptyList(),
  val defaultValue: JsonElement? = null
)

@Serializable
data class InstructionArgumentNode(
  val kind: String = "instructionArgumentNode",
  val name: String,
  val type: TypeNode,
  val docs: List<String> = emptyList(),
  val defaultValue: JsonElement? = null,
  val defaultValueStrategy: String? = null
)

@Serializable
data class AccountNode(
  val kind: String = "accountNode",
  val name: String,
  val idlName: String? = null,
  val data: TypeNode,
  val discriminators: List<DiscriminatorNode> = emptyList(),
  val docs: List<String> = emptyList(),
  val size: Int? = null
)

@Serializable
data class DefinedTypeNode(
  val kind: String = "definedTypeNode",
  val name: String,
  val idlName: String? = null,
  val type: TypeNode,
  val docs: List<String> = emptyList()
)

@Serializable
data class ErrorNode(
  val kind: String = "errorNode",
  val name: String,
  val idlName: String? = null,
  val code: Int,
  val message: String,
  val docs: List<String> = emptyList()
)

@Serializable
data class PdaNode(
  val kind: String = "pdaNode",
  val name: String,
  val seeds: List<PdaSeedNode> = emptyList()
)

@Serializable
data class PdaSeedNode(
  val kind: String,
  val name: String,
  val docs: List<String> = emptyList(),
  val type: TypeNode? = null
)

@Serializable
data class DiscriminatorNode(
  val kind: String,
  val name: String? = null,
  val offset: Int = 0,
  val size: Int? = null
)

// Type nodes - polymorphic union type
@Serializable
data class TypeNode(
  val kind: String,
  // Common fields
  val name: String? = null,
  val type: TypeNode? = null,

  // NumberTypeNode fields
  val format: String? = null,
  val endian: String? = null,

  // StructTypeNode fields
  val fields: List<StructFieldTypeNode>? = null,

  // EnumTypeNode fields
  val variants: List<EnumVariantTypeNode>? = null,
  val size: JsonElement? = null,  // Can be TypeNode or number (for fixedSizeTypeNode)

  // ArrayTypeNode fields
  val item: TypeNode? = null,
  val count: JsonElement? = null,

  // OptionTypeNode fields
  val prefix: JsonElement? = null,  // Can be TypeNode or array of nodes
  val fixed: Boolean? = null,

  // StringTypeNode fields
  val encoding: String? = null,

  // HiddenPrefixTypeNode / PreOffsetTypeNode fields
  val offset: Int? = null,
  val strategy: String? = null,

  // AmountTypeNode fields
  val decimals: Int? = null,
  val unit: String? = null,
  val number: TypeNode? = null,

  // TupleTypeNode fields
  val items: List<TypeNode>? = null,

  // MapTypeNode fields
  val key: TypeNode? = null,
  val value: TypeNode? = null,
)

@Serializable
data class StructFieldTypeNode(
  val kind: String = "structFieldTypeNode",
  val name: String,
  val type: TypeNode,
  val docs: List<String> = emptyList()
)

@Serializable
data class EnumVariantTypeNode(
  val kind: String, // enumEmptyVariantTypeNode, enumStructVariantTypeNode, enumTupleVariantTypeNode
  val name: String,
  val fields: List<StructFieldTypeNode>? = null,
  val struct: TypeNode? = null,  // for enumStructVariantTypeNode
  val tuple: TypeNode? = null,   // for enumTupleVariantTypeNode
)
