package net.avianlabs.solana.codegen

import kotlinx.serialization.json.JsonPrimitive
import net.avianlabs.solana.codegen.idl.RootNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CodamaIdlTest {

  @Test
  fun `parse minimal program IDL`() {
    val idlJson = """
      {
        "kind": "rootNode",
        "standard": "codama",
        "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "system",
          "publicKey": "11111111111111111111111111111111",
          "version": "0.0.1",
          "origin": "shank",
          "accounts": [],
          "instructions": [],
          "definedTypes": [],
          "errors": []
        },
        "additionalPrograms": []
      }
    """.trimIndent()

    val rootNode = json.decodeFromString<RootNode>(idlJson)
    assertEquals("system", rootNode.program.name)
    assertEquals("11111111111111111111111111111111", rootNode.program.publicKey)
    assertEquals("1.0.0", rootNode.version)
    assertTrue(rootNode.additionalPrograms.isEmpty())
  }

  @Test
  fun `parse program with instructions and discriminators`() {
    val idlJson = """
      {
        "kind": "rootNode",
        "standard": "codama",
        "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "system",
          "publicKey": "11111111111111111111111111111111",
          "instructions": [
            {
              "kind": "instructionNode",
              "name": "transferSol",
              "accounts": [
                {
                  "kind": "instructionAccountNode",
                  "name": "source",
                  "isWritable": true,
                  "isSigner": true,
                  "isOptional": false
                },
                {
                  "kind": "instructionAccountNode",
                  "name": "destination",
                  "isWritable": true,
                  "isSigner": false,
                  "isOptional": false
                }
              ],
              "arguments": [
                {
                  "kind": "instructionArgumentNode",
                  "name": "discriminator",
                  "type": { "kind": "numberTypeNode", "format": "u32" },
                  "defaultValue": {
                    "kind": "numberValueNode",
                    "number": 2
                  },
                  "defaultValueStrategy": "omitted"
                },
                {
                  "kind": "instructionArgumentNode",
                  "name": "amount",
                  "type": { "kind": "numberTypeNode", "format": "u64" }
                }
              ],
              "discriminators": [
                { "kind": "fieldDiscriminatorNode", "name": "discriminator" }
              ]
            }
          ]
        }
      }
    """.trimIndent()

    val rootNode = json.decodeFromString<RootNode>(idlJson)
    assertEquals(1, rootNode.program.instructions.size)

    val instruction = rootNode.program.instructions[0]
    assertEquals("transferSol", instruction.name)
    assertEquals(2, instruction.accounts.size)
    assertEquals(2, instruction.arguments.size)
    assertEquals(1, instruction.discriminators.size)
    assertEquals("discriminator", instruction.discriminators[0].name)
  }

  @Test
  fun `parse isSigner as either string`() {
    val idlJson = """
      {
        "kind": "rootNode",
        "standard": "codama",
        "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "test",
          "publicKey": "TestProgram111111111111111111111",
          "instructions": [
            {
              "kind": "instructionNode",
              "name": "testInstruction",
              "accounts": [
                {
                  "kind": "instructionAccountNode",
                  "name": "authority",
                  "isWritable": false,
                  "isSigner": "either",
                  "isOptional": false
                }
              ],
              "arguments": [],
              "discriminators": []
            }
          ]
        }
      }
    """.trimIndent()

    val rootNode = json.decodeFromString<RootNode>(idlJson)
    val account = rootNode.program.instructions[0].accounts[0]
    val signerValue = account.isSigner
    assertTrue(signerValue is JsonPrimitive)
    assertTrue(signerValue.isString)
    assertEquals("either", signerValue.content)
  }

  @Test
  fun `parse optionTypeNode with prefix`() {
    val idlJson = """
      {
        "kind": "rootNode",
        "standard": "codama",
        "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "test",
          "publicKey": "TestProgram111111111111111111111",
          "instructions": [
            {
              "kind": "instructionNode",
              "name": "testInstruction",
              "accounts": [],
              "arguments": [
                {
                  "kind": "instructionArgumentNode",
                  "name": "optionalValue",
                  "type": {
                    "kind": "optionTypeNode",
                    "prefix": { "kind": "numberTypeNode", "format": "u8" },
                    "item": { "kind": "numberTypeNode", "format": "u64" }
                  }
                }
              ],
              "discriminators": []
            }
          ]
        }
      }
    """.trimIndent()

    val rootNode = json.decodeFromString<RootNode>(idlJson)
    val arg = rootNode.program.instructions[0].arguments[0]
    assertEquals("optionTypeNode", arg.type.kind)
    val prefix = arg.type.prefix
    assertNotNull(prefix)
    assertEquals("u8", prefix.format)
    val item = arg.type.item
    assertNotNull(item)
    assertEquals("u64", item.format)
  }

  @Test
  fun `parse optionTypeNode without prefix`() {
    val idlJson = """
      {
        "kind": "rootNode",
        "standard": "codama",
        "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "test",
          "publicKey": "TestProgram111111111111111111111",
          "instructions": [
            {
              "kind": "instructionNode",
              "name": "testInstruction",
              "accounts": [],
              "arguments": [
                {
                  "kind": "instructionArgumentNode",
                  "name": "optionalValue",
                  "type": {
                    "kind": "optionTypeNode",
                    "item": { "kind": "numberTypeNode", "format": "u64" }
                  }
                }
              ],
              "discriminators": []
            }
          ]
        }
      }
    """.trimIndent()

    val rootNode = json.decodeFromString<RootNode>(idlJson)
    val arg = rootNode.program.instructions[0].arguments[0]
    assertEquals("optionTypeNode", arg.type.kind)
    assertEquals(null, arg.type.prefix)
  }

  @Test
  fun `parse definedTypeLinkNode reference`() {
    val idlJson = """
      {
        "kind": "rootNode",
        "standard": "codama",
        "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "test",
          "publicKey": "TestProgram111111111111111111111",
          "definedTypes": [
            {
              "kind": "definedTypeNode",
              "name": "authorityType",
              "type": {
                "kind": "enumTypeNode",
                "size": { "kind": "numberTypeNode", "format": "u8" },
                "variants": [
                  { "kind": "enumEmptyVariantTypeNode", "name": "mintTokens" },
                  { "kind": "enumEmptyVariantTypeNode", "name": "freezeAccount" }
                ]
              }
            }
          ],
          "instructions": [
            {
              "kind": "instructionNode",
              "name": "testInstruction",
              "accounts": [],
              "arguments": [
                {
                  "kind": "instructionArgumentNode",
                  "name": "authority",
                  "type": {
                    "kind": "definedTypeLinkNode",
                    "name": "authorityType"
                  }
                }
              ],
              "discriminators": []
            }
          ]
        }
      }
    """.trimIndent()

    val rootNode = json.decodeFromString<RootNode>(idlJson)
    val arg = rootNode.program.instructions[0].arguments[0]
    assertEquals("definedTypeLinkNode", arg.type.kind)
    assertEquals("authorityType", arg.type.name)

    // Verify the defined type resolves
    val definedType = rootNode.program.definedTypes.find { it.name == arg.type.name }
    assertNotNull(definedType)
    assertEquals("enumTypeNode", definedType.type.kind)
    assertEquals(2, definedType.type.variants?.size)
  }

  @Test
  fun `parse program with accounts and sizes`() {
    val idlJson = """
      {
        "kind": "rootNode",
        "standard": "codama",
        "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "system",
          "publicKey": "11111111111111111111111111111111",
          "accounts": [
            {
              "kind": "accountNode",
              "name": "nonce",
              "size": 80,
              "data": { "kind": "structTypeNode", "fields": [] },
              "discriminators": []
            }
          ],
          "instructions": []
        }
      }
    """.trimIndent()

    val rootNode = json.decodeFromString<RootNode>(idlJson)
    assertEquals(1, rootNode.program.accounts.size)
    assertEquals("nonce", rootNode.program.accounts[0].name)
    assertEquals(80, rootNode.program.accounts[0].size)
  }
}
