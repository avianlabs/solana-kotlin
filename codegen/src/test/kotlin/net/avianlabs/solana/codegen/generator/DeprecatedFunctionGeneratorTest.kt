package net.avianlabs.solana.codegen.generator

import net.avianlabs.solana.codegen.json
import net.avianlabs.solana.codegen.idl.RootNode
import kotlin.test.Test
import kotlin.test.assertContains

class DeprecatedFunctionGeneratorTest {

  private fun parseAndGenerate(idlJson: String): String {
    val rootNode = json.decodeFromString<RootNode>(idlJson)
    val generator = ProgramGenerator(rootNode.program)
    val fileSpec = generator.generate()
    val sb = StringBuilder()
    fileSpec.writeTo(sb)
    return sb.toString()
  }

  @Test
  fun `deprecated wrapper has Deprecated annotation with correct message`() {
    val idl = """
      {
        "kind": "rootNode", "standard": "codama", "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "system",
          "publicKey": "11111111111111111111111111111111",
          "instructions": [
            {
              "kind": "instructionNode", "name": "transferSol",
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

    // Verify @Deprecated annotation is present
    assertContains(code, "@Deprecated")
    assertContains(code, "Use transferSol instead")

    // Verify the deprecated function name
    assertContains(code, "fun transfer(")

    // Verify ReplaceWith contains correct replacement
    assertContains(code, "ReplaceWith")
    assertContains(code, "transferSol(")
  }

  @Test
  fun `deprecated wrapper contains type conversions for unsigned types`() {
    val idl = """
      {
        "kind": "rootNode", "standard": "codama", "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "system",
          "publicKey": "11111111111111111111111111111111",
          "instructions": [
            {
              "kind": "instructionNode", "name": "transferSol",
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

    // The deprecated wrapper takes Long but the new function takes ULong
    // So the conversion .toULong() should be present
    assertContains(code, "toULong()")
  }

  @Test
  fun `deprecated wrapper uses old parameter names from mapping`() {
    val idl = """
      {
        "kind": "rootNode", "standard": "codama", "version": "1.0.0",
        "program": {
          "kind": "programNode",
          "name": "system",
          "publicKey": "11111111111111111111111111111111",
          "instructions": [
            {
              "kind": "instructionNode", "name": "transferSol",
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

    // The deprecated transfer() should use the old param names from DeprecationMapper
    assertContains(code, "fromPublicKey: PublicKey")
    assertContains(code, "toPublicKey: PublicKey")
    assertContains(code, "lamports: Long")
  }

  @Test
  fun `deprecated constants are generated`() {
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

    // Verify deprecated constant aliases
    assertContains(code, "SYSVAR_RENT_ACCOUNT")
    assertContains(code, "SYSVAR_RECENT_BLOCKHASH")
    assertContains(code, "NONCE_ACCOUNT_LENGTH")
    assertContains(code, "Use RENT_SYSVAR instead")
    assertContains(code, "Use RECENT_BLOCKHASHES_SYSVAR instead")
    assertContains(code, "Use NONCE_LENGTH instead")
  }
}
