# ai agent documentation

cursor rules files for better llm understanding of the solana-kotlin codebase

## rules files

### [.cursor/rules/solana-kotlin.mdc](.cursor/rules/solana-kotlin.mdc)

main codebase overview - start here

- project structure and modules
- multiplatform target setup
- package organization
- build system and dependencies
- coding conventions (explicit api, builder pattern, etc)

### [.cursor/rules/rpc-client.mdc](.cursor/rules/rpc-client.mdc)

rpc client patterns and usage

- `SolanaClient` entry point
- extension function pattern for rpc methods
- all 14 available methods
- response handling and error types
- adding new methods

### [.cursor/rules/transactions.mdc](.cursor/rules/transactions.mdc)

transaction building and signing

- `Transaction`, `Message`, `SignedTransaction` types
- builder pattern usage
- single and multi-signer workflows
- serialization formats
- account meta handling
- common transaction patterns

### [.cursor/rules/programs.mdc](.cursor/rules/programs.mdc)

solana program integrations

- `Program` interface
- `SystemProgram` (transfers, account creation, nonces)
- `TokenProgram` and `Token2022Program` (spl tokens)
- `AssociatedTokenProgram` (ata management)
- `ComputeBudgetProgram` (compute units, priority fees)
- pda derivation
- instruction encoding patterns

### [.cursor/rules/crypto.mdc](.cursor/rules/crypto.mdc)

cryptographic primitives via tweetnacl

- ed25519 keypair generation and signing
- `PublicKey` and `Ed25519Keypair` types
- base58 encoding/decoding
- secretbox encryption (xsalsa20-poly1305)
- platform-specific implementations (jvm/ios/native)
- security best practices

## quick reference

### creating a transaction

```kotlin
val tx = Transaction.Builder()
    .addInstruction(SystemProgram.transfer(from, to, lamports))
    .setRecentBlockHash(blockhash)
    .setFeePayer(from)
    .build()
    .sign(keypair)

client.sendTransaction(tx)
```

### generating keypair

```kotlin
val keypair = Ed25519Keypair.fromBase58(secretKey)
// or
val seed = secureRandom.nextBytes(32)
val keypair = TweetNaCl.Signature.generateKey(seed)
```

### calling rpc methods

```kotlin
val client = SolanaClient(RpcKtorClient("https://api.mainnet-beta.solana.com"))
val response = client.getBalance(publicKey)
val balance = response.result
```

### token operations

```kotlin
val ata = ownerPublicKey.associatedTokenAddress(mintAddress)
val ix = TokenProgram.transfer(sourceAta, destAta, owner, amount)
```

## contributing

when adding new features:
- follow explicit api mode (`public` keyword required)
- use builder pattern for complex types
- extension functions for rpc methods
- platform-specific code via `expect/actual`
- update relevant rules file if patterns change
