# Solana Kotlin Code Generator

generates Kotlin code from Codama IDL files for Solana programs.

## Usage

```bash
# download latest IDL files from upstream
./gradlew :codegen:syncIdl

# generate all program code from IDLs
./gradlew :codegen:generateSolanaCode

# verify generated code is up-to-date (for CI)
./gradlew :codegen:checkGeneratedCode
```

## Automation

a github action (`.github/workflows/update_idl.yaml`) runs weekly to:
- sync IDLs from upstream
- regenerate code
- create PR with changes

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

automated via workflow or manually:

```bash
./gradlew :codegen:syncIdl :codegen:generateSolanaCode
```

## Architecture

- `idl/CodamaIdl.kt` - data classes for parsing Codama IDL JSON
- `generator/ProgramGenerator.kt` - main KotlinPoet code generator
- `generator/DeprecationMapper.kt` - maps old API to new (for migration)
- `generator/DeprecatedFunctionGenerator.kt` - generates backward-compat wrappers
- `GeneratePrograms.kt` - main entry point
