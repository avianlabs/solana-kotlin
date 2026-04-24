# solana-kotlin codebase overview

kotlin multiplatform library for interacting with the solana blockchain network

## project structure

### modules

**solana-kotlin** - main library
- rpc client for solana json-rpc api
- domain models (transactions, messages, accounts)
- solana program integrations (system, token, associated token, compute budget)
- package: `net.avianlabs.solana`

**tweetnacl-multiplatform** - cryptographic primitives
- ed25519 signing and keypair generation
- secretbox (xsalsa20-poly1305) encryption
- base58 encoding/decoding
- package: `net.avianlabs.solana.tweetnacl`
- uses native c bindings for ios/linux/windows, java impl for jvm

**solana-kotlin-arrow-extensions** - functional error handling
- arrow-kt integration for `Either` based error handling
- converts `Response<T>` to `Either<SolanaKotlinError, T>`
- package: `net.avianlabs.solana.arrow`

### multiplatform targets

- jvm (java 17)
- iosArm64, iosSimulatorArm64
- linuxX64
- mingwX64 (windows)

## package organization

```
net.avianlabs.solana/
├── SolanaClient.kt           # main entry point
├── client/                   # http/rpc layer
│   ├── RpcKtorClient.kt
│   ├── Response.kt
│   └── RpcError.kt
├── domain/
│   ├── core/                 # fundamental types
│   │   ├── Transaction.kt
│   │   ├── Message.kt
│   │   ├── SignedTransaction.kt
│   │   ├── PublicKey.kt
│   │   └── Signer.kt
│   └── program/              # solana programs
│       ├── Program.kt
│       ├── SystemProgram.kt
│       ├── TokenProgram.kt
│       ├── Token2022Program.kt
│       ├── AssociatedTokenProgram.kt
│       └── ComputeBudgetProgram.kt
└── methods/                  # rpc method extensions
    ├── getAccountInfo.kt
    ├── getBalance.kt
    ├── sendTransaction.kt
    └── ... (14 total)
```

## build system

- gradle with kotlin dsl
- version catalog at `gradle/libs.versions.toml`
- explicit api mode enabled (`explicitApi()`)
- maven publishing to maven central + github packages
- swift package generation for ios via skie

## key dependencies

- ktor (http client, multiplatform)
- kotlinx.serialization (json)
- kotlinx.coroutines (async)
- okio (byte streams)
- arrow-kt (functional extensions module only)

## coding conventions

### visibility modifiers

all public api requires explicit `public` keyword due to `explicitApi()` mode

### builder pattern

use `newBuilder()` methods for immutable data classes:

```kotlin
val transaction = Transaction.Builder()
    .addInstruction(instruction)
    .setRecentBlockHash(blockhash)
    .setFeePayer(payer)
    .build()
```

### extension functions

rpc methods are extension functions on `SolanaClient`:

```kotlin
suspend fun SolanaClient.getBalance(
    publicKey: PublicKey,
    commitment: Commitment = Commitment.Finalized
): Response<Long>
```

### data classes with controlled construction

use `@ConsistentCopyVisibility` with private constructors:

```kotlin
@ConsistentCopyVisibility
public data class Message private constructor(
    public val feePayer: PublicKey?,
    // ...
)
```

### platform-specific code

use `expect/actual` pattern:
- common: declare `expect` function
- jvm/ios/native: provide `actual` implementation

## testing

- commonTest for shared tests
- integration tests require actual solana rpc endpoint
- test values in `TestValues.kt`
