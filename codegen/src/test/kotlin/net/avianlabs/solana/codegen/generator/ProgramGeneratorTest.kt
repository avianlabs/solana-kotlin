package net.avianlabs.solana.codegen.generator

import net.avianlabs.solana.codegen.json
import net.avianlabs.solana.codegen.idl.ProgramNode
import net.avianlabs.solana.codegen.idl.RootNode
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProgramGeneratorTest {

  private fun parseAndGenerate(idlJson: String): String {
    val rootNode = json.decodeFromString<RootNode>(idlJson)
    val generator = ProgramGenerator(rootNode.program)
    val fileSpec = generator.generate()
    val sb = StringBuilder()
    fileSpec.writeTo(sb)
    return sb.toString()
  }

  @Test
  fun `generates program object with programId`() {
    val idl = """
      {
        "kind": "rootNode", "standard": "codama", "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "system",
          "publicKey": "11111111111111111111111111111111",
          "instructions": []
        }
      }
    """.trimIndent()

    val code = parseAndGenerate(idl)
    assertContains(code, "object SystemProgram : Program")
    assertContains(code, "PublicKey.fromBase58(\"11111111111111111111111111111111\")")
  }

  @Test
  fun `generates instruction function with correct discriminator`() {
    val idl = """
      {
        "kind": "rootNode", "standard": "codama", "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "system",
          "publicKey": "11111111111111111111111111111111",
          "instructions": [
            {
              "kind": "instructionNode",
              "name": "transferSol",
              "accounts": [
                { "kind": "instructionAccountNode", "name": "source", "isWritable": true, "isSigner": true, "isOptional": false },
                { "kind": "instructionAccountNode", "name": "destination", "isWritable": true, "isSigner": false, "isOptional": false }
              ],
              "arguments": [
                {
                  "kind": "instructionArgumentNode", "name": "discriminator",
                  "type": { "kind": "numberTypeNode", "format": "u32" },
                  "defaultValue": { "kind": "numberValueNode", "number": 2 },
                  "defaultValueStrategy": "omitted"
                },
                {
                  "kind": "instructionArgumentNode", "name": "amount",
                  "type": { "kind": "numberTypeNode", "format": "u64" }
                }
              ],
              "discriminators": [{ "kind": "fieldDiscriminatorNode", "name": "discriminator" }]
            }
          ]
        }
      }
    """.trimIndent()

    val code = parseAndGenerate(idl)
    assertContains(code, "fun transferSol(")
    assertContains(code, "source: PublicKey")
    assertContains(code, "destination: PublicKey")
    assertContains(code, "amount: ULong")
    assertContains(code, "TransferSol(2u)")
    assertContains(code, "writeIntLe(Instruction.TransferSol.index.toInt())")
    assertContains(code, "writeLongLe(amount.toLong())")
  }

  @Test
  fun `generates instruction enum with correct values`() {
    val idl = """
      {
        "kind": "rootNode", "standard": "codama", "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "system",
          "publicKey": "11111111111111111111111111111111",
          "instructions": [
            {
              "kind": "instructionNode", "name": "createAccount",
              "accounts": [],
              "arguments": [
                {
                  "kind": "instructionArgumentNode", "name": "discriminator",
                  "type": { "kind": "numberTypeNode", "format": "u32" },
                  "defaultValue": { "kind": "numberValueNode", "number": 0 },
                  "defaultValueStrategy": "omitted"
                }
              ],
              "discriminators": [{ "kind": "fieldDiscriminatorNode", "name": "discriminator" }]
            },
            {
              "kind": "instructionNode", "name": "transferSol",
              "accounts": [],
              "arguments": [
                {
                  "kind": "instructionArgumentNode", "name": "discriminator",
                  "type": { "kind": "numberTypeNode", "format": "u32" },
                  "defaultValue": { "kind": "numberValueNode", "number": 2 },
                  "defaultValueStrategy": "omitted"
                }
              ],
              "discriminators": [{ "kind": "fieldDiscriminatorNode", "name": "discriminator" }]
            }
          ]
        }
      }
    """.trimIndent()

    val code = parseAndGenerate(idl)
    assertContains(code, "enum class Instruction")
    assertContains(code, "CreateAccount(0u)")
    assertContains(code, "TransferSol(2u)")
  }

  @Test
  fun `optionTypeNode with no prefix uses writeByte (u8 default)`() {
    val idl = """
      {
        "kind": "rootNode", "standard": "codama", "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "test",
          "publicKey": "TestProgram111111111111111111111",
          "instructions": [
            {
              "kind": "instructionNode", "name": "testInstruction",
              "accounts": [],
              "arguments": [
                {
                  "kind": "instructionArgumentNode", "name": "discriminator",
                  "type": { "kind": "numberTypeNode", "format": "u8" },
                  "defaultValue": { "kind": "numberValueNode", "number": 0 },
                  "defaultValueStrategy": "omitted"
                },
                {
                  "kind": "instructionArgumentNode", "name": "optionalValue",
                  "type": {
                    "kind": "optionTypeNode",
                    "item": { "kind": "numberTypeNode", "format": "u64" }
                  }
                }
              ],
              "discriminators": [{ "kind": "fieldDiscriminatorNode", "name": "discriminator" }]
            }
          ]
        }
      }
    """.trimIndent()

    val code = parseAndGenerate(idl)
    // u8 prefix means writeByte for the option discriminator
    assertContains(code, "writeByte(1)")
    assertContains(code, "writeByte(0)")
    // Should NOT contain writeIntLe for the option prefix
    assertFalse(code.contains("writeIntLe(1)"), "Should not use writeIntLe for u8 default option prefix")
  }

  @Test
  fun `definedTypeLinkNode uses correct write method based on backing size`() {
    val idl = """
      {
        "kind": "rootNode", "standard": "codama", "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "test",
          "publicKey": "TestProgram111111111111111111111",
          "definedTypes": [
            {
              "kind": "definedTypeNode",
              "name": "myEnum",
              "type": {
                "kind": "enumTypeNode",
                "size": { "kind": "numberTypeNode", "format": "u8" },
                "variants": [
                  { "kind": "enumEmptyVariantTypeNode", "name": "optionA" },
                  { "kind": "enumEmptyVariantTypeNode", "name": "optionB" }
                ]
              }
            }
          ],
          "instructions": [
            {
              "kind": "instructionNode", "name": "testInstruction",
              "accounts": [],
              "arguments": [
                {
                  "kind": "instructionArgumentNode", "name": "discriminator",
                  "type": { "kind": "numberTypeNode", "format": "u8" },
                  "defaultValue": { "kind": "numberValueNode", "number": 0 },
                  "defaultValueStrategy": "omitted"
                },
                {
                  "kind": "instructionArgumentNode", "name": "enumValue",
                  "type": { "kind": "definedTypeLinkNode", "name": "myEnum" }
                }
              ],
              "discriminators": [{ "kind": "fieldDiscriminatorNode", "name": "discriminator" }]
            }
          ]
        }
      }
    """.trimIndent()

    val code = parseAndGenerate(idl)
    assertContains(code, ".writeByte(enumValue.value.toInt())")
  }

  @Test
  fun `definedTypeLinkNode uses writeShortLe for u16 backing`() {
    val idl = """
      {
        "kind": "rootNode", "standard": "codama", "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "test",
          "publicKey": "TestProgram111111111111111111111",
          "definedTypes": [
            {
              "kind": "definedTypeNode",
              "name": "myEnum",
              "type": {
                "kind": "enumTypeNode",
                "size": { "kind": "numberTypeNode", "format": "u16" },
                "variants": [
                  { "kind": "enumEmptyVariantTypeNode", "name": "optionA" }
                ]
              }
            }
          ],
          "instructions": [
            {
              "kind": "instructionNode", "name": "testInstruction",
              "accounts": [],
              "arguments": [
                {
                  "kind": "instructionArgumentNode", "name": "discriminator",
                  "type": { "kind": "numberTypeNode", "format": "u8" },
                  "defaultValue": { "kind": "numberValueNode", "number": 0 },
                  "defaultValueStrategy": "omitted"
                },
                {
                  "kind": "instructionArgumentNode", "name": "enumValue",
                  "type": { "kind": "definedTypeLinkNode", "name": "myEnum" }
                }
              ],
              "discriminators": [{ "kind": "fieldDiscriminatorNode", "name": "discriminator" }]
            }
          ]
        }
      }
    """.trimIndent()

    val code = parseAndGenerate(idl)
    assertContains(code, ".writeShortLe(enumValue.value.toInt())")
  }

  @Test
  fun `sysvar constants are collected correctly`() {
    val idl = """
      {
        "kind": "rootNode", "standard": "codama", "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "system",
          "publicKey": "11111111111111111111111111111111",
          "instructions": [
            {
              "kind": "instructionNode", "name": "testInstruction",
              "accounts": [
                {
                  "kind": "instructionAccountNode",
                  "name": "rentSysvar",
                  "isWritable": false,
                  "isSigner": false,
                  "isOptional": false,
                  "defaultValue": {
                    "kind": "publicKeyValueNode",
                    "publicKey": "SysvarRent111111111111111111111111111111111"
                  }
                }
              ],
              "arguments": [
                {
                  "kind": "instructionArgumentNode", "name": "discriminator",
                  "type": { "kind": "numberTypeNode", "format": "u32" },
                  "defaultValue": { "kind": "numberValueNode", "number": 0 },
                  "defaultValueStrategy": "omitted"
                }
              ],
              "discriminators": [{ "kind": "fieldDiscriminatorNode", "name": "discriminator" }]
            }
          ]
        }
      }
    """.trimIndent()

    val code = parseAndGenerate(idl)
    assertContains(code, "RENT_SYSVAR")
    assertContains(code, "SysvarRent111111111111111111111111111111111")
  }

  @Test
  fun `token program generates delegating public methods and internal methods`() {
    val idl = """
      {
        "kind": "rootNode", "standard": "codama", "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "splToken",
          "publicKey": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA",
          "instructions": [
            {
              "kind": "instructionNode", "name": "transfer",
              "accounts": [
                { "kind": "instructionAccountNode", "name": "source", "isWritable": true, "isSigner": false, "isOptional": false },
                { "kind": "instructionAccountNode", "name": "destination", "isWritable": true, "isSigner": false, "isOptional": false },
                { "kind": "instructionAccountNode", "name": "authority", "isWritable": false, "isSigner": true, "isOptional": false }
              ],
              "arguments": [
                {
                  "kind": "instructionArgumentNode", "name": "discriminator",
                  "type": { "kind": "numberTypeNode", "format": "u8" },
                  "defaultValue": { "kind": "numberValueNode", "number": 3 },
                  "defaultValueStrategy": "omitted"
                },
                {
                  "kind": "instructionArgumentNode", "name": "amount",
                  "type": { "kind": "numberTypeNode", "format": "u64" }
                }
              ],
              "discriminators": [{ "kind": "fieldDiscriminatorNode", "name": "discriminator" }]
            }
          ]
        }
      }
    """.trimIndent()

    val code = parseAndGenerate(idl)

    // Public method delegates to internal
    assertContains(code, "public fun transfer(")
    assertContains(code, "createTransferInstruction(")
    assertContains(code, "programId = programId)")

    // Internal method has the actual body
    assertContains(code, "internal fun createTransferInstruction(")
    assertContains(code, "createTransactionInstruction(")
  }

  @Test
  fun `generates defined enum type`() {
    val idl = """
      {
        "kind": "rootNode", "standard": "codama", "version": "1.0.0",
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
                  { "kind": "enumEmptyVariantTypeNode", "name": "freezeAccount" },
                  { "kind": "enumEmptyVariantTypeNode", "name": "accountOwner" }
                ]
              }
            }
          ],
          "instructions": []
        }
      }
    """.trimIndent()

    val code = parseAndGenerate(idl)
    assertContains(code, "enum class AuthorityType")
    assertContains(code, "MintTokens(0u)")
    assertContains(code, "FreezeAccount(1u)")
    assertContains(code, "AccountOwner(2u)")
  }

  @Test
  fun `generates account size constants`() {
    val idl = """
      {
        "kind": "rootNode", "standard": "codama", "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "test",
          "publicKey": "TestProgram111111111111111111111",
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

    val code = parseAndGenerate(idl)
    assertContains(code, "NONCE_LENGTH")
    assertContains(code, "80L")
  }

  @Test
  fun `isSigner either is treated as signer`() {
    val idl = """
      {
        "kind": "rootNode", "standard": "codama", "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "test",
          "publicKey": "TestProgram111111111111111111111",
          "instructions": [
            {
              "kind": "instructionNode", "name": "testInstruction",
              "accounts": [
                {
                  "kind": "instructionAccountNode",
                  "name": "authority",
                  "isWritable": false,
                  "isSigner": "either",
                  "isOptional": false
                }
              ],
              "arguments": [
                {
                  "kind": "instructionArgumentNode", "name": "discriminator",
                  "type": { "kind": "numberTypeNode", "format": "u8" },
                  "defaultValue": { "kind": "numberValueNode", "number": 0 },
                  "defaultValueStrategy": "omitted"
                }
              ],
              "discriminators": [{ "kind": "fieldDiscriminatorNode", "name": "discriminator" }]
            }
          ]
        }
      }
    """.trimIndent()

    val code = parseAndGenerate(idl)
    assertContains(code, "AccountMeta(authority, isSigner = true, isWritable = false)")
  }
}
