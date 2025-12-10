# Solana Kotlin Code Generator

generates Kotlin code from Codama IDL files for Solana programs.

## Usage

```bash
# generate all program code
./gradlew :codegen:generateSolanaCode

# verify generated code is up-to-date (for CI)
./gradlew :codegen:checkGeneratedCode
```

## Source of Truth

IDL files are downloaded from official Solana program repos and stored in `codegen/idl/`:

- `system.json` - System Program
- `token.json` - Token Program + Associated Token Program (in additionalPrograms)
- `compute-budget.json` - Compute Budget Program

## Generated Files

The following files are generated and should NOT be manually edited:

- `solana-kotlin/src/commonMain/kotlin/net/avianlabs/solana/domain/program/SystemProgram.kt`
- `solana-kotlin/src/commonMain/kotlin/net/avianlabs/solana/domain/program/TokenProgram.kt`
- `solana-kotlin/src/commonMain/kotlin/net/avianlabs/solana/domain/program/AssociatedTokenProgram.kt`
- `solana-kotlin/src/commonMain/kotlin/net/avianlabs/solana/domain/program/ComputeBudgetProgram.kt`

## Updating IDLs

to update to the latest IDLs:

```bash
cd codegen/idl

# system program
curl -sL https://raw.githubusercontent.com/solana-program/system/main/program/idl.json -o system.json

# token program (includes ATA)
curl -sL https://raw.githubusercontent.com/solana-program/token/main/program/idl.json -o token.json

# compute budget
curl -sL https://raw.githubusercontent.com/solana-program/compute-budget/main/program/idl.json -o compute-budget.json
```

then regenerate:

```bash
./gradlew :codegen:generateSolanaCode
```

## Architecture

- `idl/CodamaIdl.kt` - data classes for parsing Codama IDL JSON
- `generator/ProgramGenerator.kt` - KotlinPoet-based code generator
- `GeneratePrograms.kt` - main entry point
