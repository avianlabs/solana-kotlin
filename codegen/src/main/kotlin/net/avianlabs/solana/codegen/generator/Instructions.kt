package net.avianlabs.solana.codegen.generator

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.avianlabs.solana.codegen.idl.InstructionAccountNode
import net.avianlabs.solana.codegen.idl.InstructionNode
import net.avianlabs.solana.codegen.idl.TypeNode

fun InstructionAccountNode.hasPublicKeyDefault(): Boolean =
  defaultValue?.let { defaultValue ->
    (defaultValue as? JsonObject)?.let { obj ->
      obj["kind"]?.jsonPrimitive?.content == "publicKeyValueNode"
    } ?: false
  } ?: false

fun InstructionAccountNode.getPublicKeyDefault(): String? =
  defaultValue?.let { defaultValue ->
    (defaultValue as? JsonObject)?.let { obj ->
      if (obj["kind"]?.jsonPrimitive?.content == "publicKeyValueNode") {
        obj["publicKey"]?.jsonPrimitive?.content
      } else null
    }
  }

/**
 * An instruction whose argument types include constructs the codegen cannot
 * map to typed Kotlin (e.g. an inline anonymous struct nested inside an array).
 * These would otherwise produce non-compiling code, so they are skipped.
 */
fun InstructionNode.hasUnsupportedTypes(): Boolean =
  arguments.any { it.type.containsUnsupportedConstruct() }

private fun TypeNode.containsUnsupportedConstruct(): Boolean = when (kind) {
  "arrayTypeNode" -> {
    val itemType = item
    itemType?.kind == "structTypeNode" || itemType?.containsUnsupportedConstruct() == true
  }
  "optionTypeNode", "zeroableOptionTypeNode", "remainderOptionTypeNode" ->
    item?.containsUnsupportedConstruct() == true
  "sizePrefixTypeNode", "fixedSizeTypeNode", "hiddenPrefixTypeNode", "preOffsetTypeNode" ->
    type?.containsUnsupportedConstruct() == true
  "amountTypeNode" -> number?.containsUnsupportedConstruct() == true
  "tupleTypeNode" -> items?.any { it.containsUnsupportedConstruct() } == true
  "mapTypeNode" ->
    (key?.containsUnsupportedConstruct() == true) || (value?.containsUnsupportedConstruct() == true)
  else -> false
}

/**
 * Returns true if two instructions would generate the same Kotlin parameter list
 * (same parameter count and types, in the same order) for the abstract method
 * declared on a shared sealed parent class.
 */
fun signaturesCompatible(a: InstructionNode, b: InstructionNode): Boolean {
  val aNonDef = a.accounts.count { !it.hasPublicKeyDefault() }
  val aDef = a.accounts.count { it.hasPublicKeyDefault() }
  val bNonDef = b.accounts.count { !it.hasPublicKeyDefault() }
  val bDef = b.accounts.count { it.hasPublicKeyDefault() }
  if (aNonDef != bNonDef || aDef != bDef) return false

  val aArgs = a.arguments.filter { arg -> a.discriminators.none { it.name == arg.name } }
  val bArgs = b.arguments.filter { arg -> b.discriminators.none { it.name == arg.name } }
  if (aArgs.size != bArgs.size) return false

  return aArgs.zip(bArgs).all { (x, y) -> typeNodesEquivalent(x.type, y.type) }
}

private fun typeNodesEquivalent(a: TypeNode, b: TypeNode): Boolean {
  if (a.kind != b.kind) return false
  if (a.format != b.format) return false
  if (a.name != b.name) return false
  if ((a.item == null) != (b.item == null)) return false
  if (a.item != null && b.item != null && !typeNodesEquivalent(a.item, b.item)) return false
  if ((a.type == null) != (b.type == null)) return false
  if (a.type != null && b.type != null && !typeNodesEquivalent(a.type, b.type)) return false
  if ((a.number == null) != (b.number == null)) return false
  if (a.number != null && b.number != null && !typeNodesEquivalent(a.number, b.number)) return false
  if ((a.items == null) != (b.items == null)) return false
  if (a.items != null && b.items != null) {
    if (a.items.size != b.items.size) return false
    if (!a.items.zip(b.items).all { (x, y) -> typeNodesEquivalent(x, y) }) return false
  }
  return true
}
