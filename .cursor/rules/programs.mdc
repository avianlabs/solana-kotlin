# solana program interactions

## Program interface

all programs implement `Program`:

```kotlin
interface Program {
    val programId: PublicKey
    
    companion object {
        fun createTransactionInstruction(
            programId: PublicKey,
            keys: List<AccountMeta>,
            data: ByteArray
        ): TransactionInstruction
        
        fun findProgramAddress(
            seeds: List<ByteArray>,
            programId: PublicKey
        ): ProgramDerivedAddress
    }
}
```

## SystemProgram

solana native system program for basic operations

program id: `11111111111111111111111111111111`

### transfer lamports

```kotlin
val ix = SystemProgram.transfer(
    fromPublicKey = sender,
    toPublicKey = recipient,
    lamports = 1_000_000 // 0.001 SOL
)
```

### create account

```kotlin
val ix = SystemProgram.createAccount(
    fromPublicKey = payer,
    newAccountPublicKey = newAccount,
    lamports = rentExemptAmount,
    space = 165 // bytes
)
```

### nonce operations

durable transaction nonces for offline signing

initialize:

```kotlin
val ix = SystemProgram.nonceInitialize(
    nonceAccount = nonceAccount,
    authorized = authority
)
```

advance:

```kotlin
val ix = SystemProgram.nonceAdvance(
    nonceAccount = nonceAccount,
    authorized = authority
)
```

withdraw:

```kotlin
val ix = SystemProgram.nonceWithdraw(
    nonceAccount = nonceAccount,
    authorized = authority,
    toPublicKey = recipient,
    lamports = amount
)
```

authorize:

```kotlin
val ix = SystemProgram.nonceAuthorize(
    nonceAccount = nonceAccount,
    authorized = currentAuthority,
    newAuthorized = newAuthority
)
```

### constants

```kotlin
SystemProgram.SYSVAR_RENT_ACCOUNT
SystemProgram.SYSVAR_RECENT_BLOCKHASH
SystemProgram.NONCE_ACCOUNT_LENGTH // 80 bytes
```

## TokenProgram

spl token program for fungible tokens

program id: `TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA`

### transfer tokens

```kotlin
val ix = TokenProgram.transfer(
    source = sourceTokenAccount,
    destination = destTokenAccount,
    owner = ownerPublicKey,
    amount = 1000u
)
```

### transfer checked

includes mint and decimals for validation:

```kotlin
val ix = TokenProgram.transferChecked(
    source = sourceTokenAccount,
    mint = mintAddress,
    destination = destTokenAccount,
    owner = ownerPublicKey,
    amount = 1000u,
    decimals = 6u
)
```

### approve delegate

```kotlin
val ix = TokenProgram.approve(
    source = tokenAccount,
    delegate = delegatePublicKey,
    owner = ownerPublicKey,
    amount = 1000u
)
```

### revoke approval

```kotlin
val ix = TokenProgram.revoke(
    source = tokenAccount,
    owner = ownerPublicKey
)
```

### close account

```kotlin
val ix = TokenProgram.closeAccount(
    account = tokenAccount,
    destination = solDestination,
    owner = ownerPublicKey
)
```

## Token2022Program

token extensions program (token-2022)

program id: `TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb`

implements same `TokenProgram` interface with different program id:

```kotlin
val ix = Token2022Program.transfer(
    source = source,
    destination = dest,
    owner = owner,
    amount = amount
)
```

## AssociatedTokenProgram

creates deterministic token account addresses

program id: `ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL`

### create associated token account

```kotlin
val ix = AssociatedTokenProgram.createAssociatedTokenAccountInstruction(
    associatedProgramId = AssociatedTokenProgram.programId,
    programId = TokenProgram.programId,
    mint = mintAddress,
    associatedAccount = ataAddress,
    owner = ownerPublicKey,
    payer = payerPublicKey
)
```

### idempotent creation

won't fail if account already exists:

```kotlin
val ix = AssociatedTokenProgram.createAssociatedTokenAccountInstructionIdempotent(
    associatedProgramId = AssociatedTokenProgram.programId,
    programId = TokenProgram.programId,
    mint = mintAddress,
    associatedAccount = ataAddress,
    owner = ownerPublicKey,
    payer = payerPublicKey
)
```

### find associated token address

extension function on `PublicKey`:

```kotlin
val ata: ProgramDerivedAddress = ownerPublicKey.associatedTokenAddress(
    tokenMintAddress = mintAddress,
    programId = TokenProgram.programId
)

val ataAddress = ata.address
```

## ComputeBudgetProgram

manage compute unit limits and priority fees

program id: `ComputeBudget111111111111111111111111111111`

### set compute unit limit

```kotlin
val ix = ComputeBudgetProgram.setComputeUnitLimit(units = 200_000u)
```

### set compute unit price

priority fee in micro-lamports per compute unit:

```kotlin
val ix = ComputeBudgetProgram.setComputeUnitPrice(
    microLamports = 1000u
)
```

### request heap frame

```kotlin
val ix = ComputeBudgetProgram.requestHeapFrame(bytes = 32_768u)
```

## ProgramDerivedAddress (PDA)

deterministic addresses derived from seeds

```kotlin
data class ProgramDerivedAddress(
    val address: PublicKey,
    val nonce: UByte
)
```

find pda:

```kotlin
val pda = Program.findProgramAddress(
    seeds = listOf(
        "metadata".encodeToByteArray(),
        programId.bytes,
        mintAddress.bytes
    ),
    programId = metadataProgramId
)
```

pda is valid public key not on ed25519 curve (no private key exists)

## instruction data encoding

programs use okio `Buffer` for binary encoding:

```kotlin
val data = Buffer()
    .writeIntLe(instructionIndex)
    .writeLongLe(amount)
    .write(publicKey.bytes)
    .readByteArray()
```

pattern:
1. write instruction discriminator (u32 le)
2. write params in order (le for numbers)
3. extract byte array

## account meta patterns

### signer + writable

```kotlin
AccountMeta(publicKey, isSigner = true, isWritable = true)
```

fee payers and authorities

### writable only

```kotlin
AccountMeta(publicKey, isSigner = false, isWritable = true)
```

accounts being modified (balances, data)

### read-only

```kotlin
AccountMeta(publicKey, isSigner = false, isWritable = false)
```

programs, mint addresses, read-only data

## multi-instruction patterns

### create ata + transfer

```kotlin
val tx = Transaction.Builder()
    .addInstruction(
        AssociatedTokenProgram.createAssociatedTokenAccountInstructionIdempotent(...)
    )
    .addInstruction(
        TokenProgram.transfer(...)
    )
    .setRecentBlockHash(blockhash)
    .setFeePayer(payer)
    .build()
```

### compute budget + main instruction

```kotlin
val tx = Transaction.Builder()
    .addInstruction(
        ComputeBudgetProgram.setComputeUnitPrice(1000u)
    )
    .addInstruction(
        ComputeBudgetProgram.setComputeUnitLimit(200_000u)
    )
    .addInstruction(mainInstruction)
    .setRecentBlockHash(blockhash)
    .build()
```

## custom programs

for custom program instructions:

```kotlin
val customInstruction = Program.createTransactionInstruction(
    programId = PublicKey.fromBase58("YourProgramId..."),
    keys = listOf(
        AccountMeta(account1, isSigner = true, isWritable = true),
        AccountMeta(account2, isSigner = false, isWritable = true),
        AccountMeta(programId, isSigner = false, isWritable = false)
    ),
    data = Buffer()
        .writeByte(instructionDiscriminator)
        .writeLongLe(param1)
        .write(param2Bytes)
        .readByteArray()
)
```

## token amounts

token amounts use native units (smallest denomination):

```kotlin
// for a token with 6 decimals
val oneToken = 1_000_000u // 1.0 token
val halfToken = 500_000u  // 0.5 tokens
```

usdc example (6 decimals):
- 1 usdc = 1_000_000 units
- 0.01 usdc = 10_000 units

## error handling

program errors come through rpc simulation:

```kotlin
val simResult = client.simulateTransaction(signedTx)
if (simResult.error != null) {
    // check logs for program errors
    val logs = simResult.result?.logs
}
```

common program errors:
- insufficient funds
- account already initialized
- account not owned by program
- invalid instruction data
- missing required signature
