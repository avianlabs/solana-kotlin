package net.avianlabs.solana.codegen.generator

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.avianlabs.solana.codegen.idl.InstructionAccountNode

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
